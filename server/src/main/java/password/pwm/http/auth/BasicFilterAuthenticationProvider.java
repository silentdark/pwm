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

package password.pwm.http.auth;

import password.pwm.PwmDomain;
import password.pwm.bean.UserIdentity;
import password.pwm.config.PwmSetting;
import password.pwm.error.PwmError;
import password.pwm.error.PwmException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.http.PwmRequest;
import password.pwm.http.PwmSession;
import password.pwm.ldap.auth.PwmAuthenticationSource;
import password.pwm.ldap.auth.SessionAuthenticator;
import password.pwm.ldap.search.UserSearchEngine;
import password.pwm.svc.stats.Statistic;
import password.pwm.svc.stats.StatisticsClient;
import password.pwm.util.BasicAuthInfo;
import password.pwm.util.logging.PwmLogger;

import java.util.Optional;

public class BasicFilterAuthenticationProvider implements PwmHttpFilterAuthenticationProvider
{
    private static final PwmLogger LOGGER = PwmLogger.forClass( BasicFilterAuthenticationProvider.class );

    @Override
    public void attemptAuthentication(
            final PwmRequest pwmRequest
    )
            throws PwmUnrecoverableException
    {
        if ( !pwmRequest.getDomainConfig().readSettingAsBoolean( PwmSetting.BASIC_AUTH_ENABLED ) )
        {
            return;
        }

        if ( pwmRequest.isAuthenticated() )
        {
            return;
        }

        final Optional<BasicAuthInfo> basicAuthInfo = BasicAuthInfo.parseAuthHeader( pwmRequest.getPwmDomain(), pwmRequest );
        if ( basicAuthInfo.isEmpty() )
        {
            return;
        }

        try
        {
            final PwmSession pwmSession = pwmRequest.getPwmSession();
            final PwmDomain pwmDomain = pwmRequest.getPwmDomain();

            //user isn't already authenticated and has an auth header, so try to auth them.
            LOGGER.debug( pwmRequest, () -> "attempting to authenticate user using basic auth header (username=" + basicAuthInfo.get().getUsername() + ")" );
            final SessionAuthenticator sessionAuthenticator = new SessionAuthenticator(
                    pwmDomain,
                    pwmRequest,
                    PwmAuthenticationSource.BASIC_AUTH
            );
            final UserSearchEngine userSearchEngine = pwmDomain.getUserSearchEngine();
            final UserIdentity userIdentity = userSearchEngine.resolveUsername( basicAuthInfo.get().getUsername(), null, null, pwmRequest.getLabel() );
            sessionAuthenticator.authenticateUser( userIdentity, basicAuthInfo.get().getPassword() );
            pwmSession.getLoginInfoBean().setBasicAuth( basicAuthInfo.get() );
        }
        catch ( final PwmException e )
        {
            if ( e.getError() == PwmError.ERROR_DIRECTORY_UNAVAILABLE )
            {
                StatisticsClient.incrementStat( pwmRequest, Statistic.LDAP_UNAVAILABLE_COUNT );
            }
            throw new PwmUnrecoverableException( e.getError() );
        }
    }

    @Override
    public boolean hasRedirectedResponse( )
    {
        return false;
    }
}
