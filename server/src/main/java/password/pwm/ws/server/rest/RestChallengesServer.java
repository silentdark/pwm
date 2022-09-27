/*
 * Password Management Servlets (PWM)
 * http://www.pwm-project.org
 *
 * Copyright (c) 2006-2009 Novell, Inc.
 * Copyright (c) 2009-2021 The PWM Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package password.pwm.ws.server.rest;

import com.novell.ldapchai.ChaiUser;
import com.novell.ldapchai.cr.ChaiChallenge;
import com.novell.ldapchai.cr.Challenge;
import com.novell.ldapchai.cr.ResponseSet;
import com.novell.ldapchai.cr.bean.ChallengeBean;
import com.novell.ldapchai.exception.ChaiException;
import lombok.Data;
import password.pwm.PwmConstants;
import password.pwm.bean.ResponseInfoBean;
import password.pwm.bean.UserIdentity;
import password.pwm.config.PwmSetting;
import password.pwm.config.option.WebServiceUsage;
import password.pwm.config.profile.ChallengeProfile;
import password.pwm.config.profile.PwmPasswordPolicy;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmError;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.http.HttpContentType;
import password.pwm.http.HttpMethod;
import password.pwm.http.PwmHttpRequestWrapper;
import password.pwm.i18n.Message;
import password.pwm.ldap.LdapOperationsHelper;
import password.pwm.svc.cr.CrService;
import password.pwm.svc.stats.Statistic;
import password.pwm.svc.stats.StatisticsClient;
import password.pwm.util.password.PasswordUtility;
import password.pwm.ws.server.RestMethodHandler;
import password.pwm.ws.server.RestRequest;
import password.pwm.ws.server.RestResultBean;
import password.pwm.ws.server.RestServlet;
import password.pwm.ws.server.RestUtility;
import password.pwm.ws.server.RestWebServer;

import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet(
        urlPatterns = {
                PwmConstants.URL_PREFIX_PUBLIC + PwmConstants.URL_PREFIX_REST + "/challenges"
        }
)
@RestWebServer( webService = WebServiceUsage.CheckPassword )
public class RestChallengesServer extends RestServlet
{

    private static final String FIELD_USERNAME = "username";
    private static final String FIELD_ANSWERS = "answers";
    private static final String FIELD_HELPDESK = "helpdesk";

    @Data
    public static class Policy implements Serializable
    {
        public List<ChallengeBean> challenges;
        public List<ChallengeBean> helpdeskChallenges;
        public int minimumRandoms;
    }

    @Data
    private static class JsonDeleteInput implements Serializable
    {
        private String username;
    }

    @Data
    public static class JsonChallengesData implements Serializable
    {
        public String username;
        public List<ChallengeBean> challenges;
        public List<ChallengeBean> helpdeskChallenges;
        public int minimumRandoms;
        public Policy policy;

        public ResponseInfoBean toResponseInfoBean( final Locale locale, final String csIdentifier )
                throws PwmOperationalException
        {
            final Map<Challenge, String> crMap = new LinkedHashMap<>();
            if ( challenges != null )
            {
                for ( final ChallengeBean challengeBean : challenges )
                {
                    final Challenge challenge = ChaiChallenge.fromChallengeBean( challengeBean );
                    final String answerText = challengeBean.getAnswer().getAnswerText();
                    if ( answerText == null || answerText.length() < 1 )
                    {
                        throw new IllegalArgumentException( "missing answerText for challenge '" + challenge.getChallengeText() + "'" );
                    }
                    crMap.put( challenge, answerText );
                }
            }

            final Map<Challenge, String> helpdeskCrMap = new LinkedHashMap<>();
            if ( helpdeskChallenges != null )
            {
                for ( final ChallengeBean challengeBean : helpdeskChallenges )
                {
                    final Challenge challenge = ChaiChallenge.fromChallengeBean( challengeBean );
                    final String answerText = challengeBean.getAnswer().getAnswerText();
                    if ( answerText == null || answerText.length() < 1 )
                    {
                        throw new IllegalArgumentException( "missing answerText for helpdesk challenge '" + challenge.getChallengeText() + "'" );
                    }
                    helpdeskCrMap.put( challenge, answerText );
                }
            }

            final ResponseInfoBean responseInfoBean = new ResponseInfoBean(
                    crMap,
                    helpdeskCrMap,
                    locale,
                    minimumRandoms,
                    csIdentifier,
                    null,
                    null
            );
            responseInfoBean.setTimestamp( Instant.now() );
            return responseInfoBean;
        }
    }

    @Override
    public void preCheckRequest( final RestRequest request ) throws PwmUnrecoverableException
    {
    }

    @RestMethodHandler( method = HttpMethod.GET, produces = HttpContentType.json )
    public RestResultBean doFormGetChallengeData( final RestRequest restRequest )

            throws PwmUnrecoverableException
    {
        final boolean answers = restRequest.readParameterAsBoolean( FIELD_ANSWERS );
        final boolean helpdesk = restRequest.readParameterAsBoolean( FIELD_HELPDESK );
        final String username = restRequest.readParameterAsString( FIELD_USERNAME, PwmHttpRequestWrapper.Flag.BypassValidation );

        try
        {
            if ( answers && !restRequest.getDomain().getConfig().readSettingAsBoolean( PwmSetting.ENABLE_WEBSERVICES_READANSWERS ) )
            {
                throw new PwmUnrecoverableException( new ErrorInformation( PwmError.ERROR_SERVICE_NOT_AVAILABLE, "retrieval of answers is not permitted" ) );
            }

            final TargetUserIdentity targetUserIdentity = RestUtility.resolveRequestedUsername( restRequest, username );

            // gather data
            final ResponseSet responseSet;
            final String outputUsername;

            final ChaiUser chaiUser = targetUserIdentity.getChaiUser();
            final Locale userLocale = restRequest.getLocale();
            final CrService crService = restRequest.getDomain().getCrService();
            responseSet = crService.readUserResponseSet( restRequest.getSessionLabel(), targetUserIdentity.getUserIdentity(), chaiUser ).orElseThrow();

            final PwmPasswordPolicy passwordPolicy = PasswordUtility.readPasswordPolicyForUser(
                    restRequest.getDomain(),
                    restRequest.getSessionLabel(),
                    targetUserIdentity.getUserIdentity(),
                    chaiUser );
            final ChallengeProfile challengeProfile = crService.readUserChallengeProfile(
                    restRequest.getSessionLabel(),
                    targetUserIdentity.getUserIdentity(),
                    chaiUser,
                    passwordPolicy,
                    userLocale
            );

            outputUsername = targetUserIdentity.getUserIdentity().toDelimitedKey();

            // build output
            final JsonChallengesData jsonData = new JsonChallengesData();
            {
                jsonData.username = outputUsername;
                if ( responseSet != null )
                {
                    jsonData.challenges = responseSet.asChallengeBeans( answers );
                    if ( helpdesk )
                    {
                        jsonData.helpdeskChallenges = responseSet.asHelpdeskChallengeBeans( answers );
                    }
                    jsonData.minimumRandoms = responseSet.getChallengeSet().getMinRandomRequired();
                }
                final Policy policy = new Policy();

                challengeProfile.getChallengeSet().ifPresent( challengeSet ->
                {
                    policy.challenges = challengesToBeans( challengeSet.getChallenges() );
                    policy.minimumRandoms = challengeSet.getMinRandomRequired();
                } );

                challengeProfile.getHelpdeskChallengeSet().ifPresent( helpdeskChallengeSet ->
                {
                    policy.helpdeskChallenges = challengesToBeans( helpdeskChallengeSet.getChallenges() );
                } );

                if ( policy.challenges != null || policy.helpdeskChallenges != null )
                {
                    jsonData.policy = policy;
                }
            }

            // update statistics
            StatisticsClient.incrementStat( restRequest.getDomain(), Statistic.REST_CHALLENGES );
            return RestResultBean.withData( jsonData, JsonChallengesData.class );
        }
        catch ( final ChaiException e )
        {
            final String errorMsg = "unexpected error building json response: " + e.getMessage();
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_INTERNAL, errorMsg );
            return RestResultBean.fromError( restRequest, errorInformation );
        }
    }

    @RestMethodHandler( method = HttpMethod.POST, consumes = HttpContentType.json, produces = HttpContentType.json )
    public RestResultBean doSetChallengeDataJson( final RestRequest restRequest )
            throws  PwmUnrecoverableException
    {
        final JsonChallengesData jsonInput = RestUtility.deserializeJsonBody( restRequest, JsonChallengesData.class );

        final String username = RestUtility.readValueFromJsonAndParam(
                jsonInput.getUsername(),
                restRequest.readParameterAsString( FIELD_USERNAME, PwmHttpRequestWrapper.Flag.BypassValidation ),
                FIELD_USERNAME,
                RestUtility.ReadValueFlag.optional
        ).orElseThrow( () -> PwmUnrecoverableException.newException( PwmError.ERROR_FIELD_REQUIRED, FIELD_USERNAME ) );

        final TargetUserIdentity targetUserIdentity = RestUtility.resolveRequestedUsername( restRequest, username );

        try
        {
            final ChaiUser chaiUser;
            final String userGUID;
            final String csIdentifer;
            final UserIdentity userIdentity;
            final CrService crService = restRequest.getDomain().getCrService();

            userIdentity = targetUserIdentity.getUserIdentity();
            chaiUser = targetUserIdentity.getChaiUser();
            userGUID = LdapOperationsHelper.readLdapGuidValue(
                    restRequest.getDomain(),
                    restRequest.getSessionLabel(),
                    userIdentity,
                    false
            );
            final ChallengeProfile challengeProfile = crService.readUserChallengeProfile(
                    restRequest.getSessionLabel(),
                    userIdentity,
                    chaiUser,
                    PwmPasswordPolicy.defaultPolicy(),
                    restRequest.getLocale()
            );

            csIdentifer = challengeProfile.getChallengeSet()
                    .orElseThrow( () -> new PwmUnrecoverableException( PwmError.ERROR_NO_CHALLENGES.toInfo() ) )
                    .getIdentifier();

            final ResponseInfoBean responseInfoBean = jsonInput.toResponseInfoBean( restRequest.getLocale(), csIdentifer );
            crService.writeResponses( restRequest.getSessionLabel(), userIdentity, chaiUser, userGUID, responseInfoBean );

            // update statistics
            StatisticsClient.incrementStat( restRequest.getDomain(), Statistic.REST_CHALLENGES );

            return RestResultBean.forSuccessMessage( restRequest, Message.Success_SetupResponse );
        }
        catch ( final Exception e )
        {
            final String errorMsg = "unexpected error reading json input: " + e.getMessage();
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_INTERNAL, errorMsg );
            return RestResultBean.fromError( restRequest, errorInformation );
        }
    }

    @RestMethodHandler( method = HttpMethod.DELETE, produces = HttpContentType.json )
    public RestResultBean processJsonDeleteChallengeData( final RestRequest restRequest )
            throws IOException, PwmUnrecoverableException
    {
        final String username = restRequest.readParameterAsString( FIELD_USERNAME );

        return doDeleteChallengeData( restRequest, username );
    }

    private RestResultBean doDeleteChallengeData( final RestRequest restRequest, final String username )
            throws PwmUnrecoverableException
    {

        final TargetUserIdentity targetUserIdentity = RestUtility.resolveRequestedUsername( restRequest, username );

        try
        {
            final ChaiUser chaiUser;
            final String userGUID;

            chaiUser = targetUserIdentity.getChaiUser();
            userGUID = LdapOperationsHelper.readLdapGuidValue(
                    restRequest.getDomain(),
                    restRequest.getSessionLabel(),
                    targetUserIdentity.getUserIdentity(),
                    false );

            final CrService crService = restRequest.getDomain().getCrService();
            crService.clearResponses(
                    restRequest.getSessionLabel(),
                    targetUserIdentity.getUserIdentity(),
                    chaiUser,
                    userGUID
            );

            // update statistics
            StatisticsClient.incrementStat( restRequest.getDomain(), Statistic.REST_CHALLENGES );

            return RestResultBean.forSuccessMessage( restRequest, Message.Success_Unknown );
        }
        catch ( final Exception e )
        {
            final String errorMsg = "unexpected error delete responses: " + e.getMessage();
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_INTERNAL, errorMsg );
            return RestResultBean.fromError( restRequest, errorInformation );
        }
    }

    private static List<ChallengeBean> challengesToBeans( final List<Challenge> challenges )
    {
        if ( challenges == null )
        {
            return Collections.emptyList();
        }

        return challenges.stream()
                .map( Challenge::asChallengeBean )
                .collect( Collectors.toUnmodifiableList() );
    }
}
