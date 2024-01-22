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

package password.pwm.ws.server;

import com.novell.ldapchai.provider.ChaiProvider;
import password.pwm.PwmApplication;
import password.pwm.PwmDomain;
import password.pwm.bean.ProfileID;
import password.pwm.bean.SessionLabel;
import password.pwm.error.PwmError;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.http.PwmRequestContext;
import password.pwm.http.HttpContentType;
import password.pwm.http.HttpHeader;
import password.pwm.http.PwmHttpRequestWrapper;
import password.pwm.http.servlet.PwmRequestID;
import password.pwm.util.i18n.LocaleHelper;
import password.pwm.util.logging.PwmLogger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class RestRequest extends PwmHttpRequestWrapper
{
    private static final PwmLogger LOGGER = PwmLogger.forClass( RestRequest.class );

    private final PwmDomain pwmDomain;
    private final RestAuthentication restAuthentication;
    private final SessionLabel sessionLabel;
    private final PwmRequestID requestID;

    public static RestRequest forRequest(
            final PwmDomain pwmDomain,
            final RestAuthentication restAuthentication,
            final SessionLabel sessionLabel,
            final HttpServletRequest httpServletRequest
    )
            throws PwmUnrecoverableException
    {
        return new RestRequest( pwmDomain, restAuthentication, sessionLabel, httpServletRequest );
    }

    private RestRequest(
            final PwmDomain pwmDomain,
            final RestAuthentication restAuthentication,
            final SessionLabel sessionLabel,
            final HttpServletRequest httpServletRequest
    )
            throws PwmUnrecoverableException
    {
        super( httpServletRequest, pwmDomain.getConfig().getAppConfig() );
        this.pwmDomain = pwmDomain;
        this.restAuthentication = restAuthentication;
        this.sessionLabel = sessionLabel;
        this.requestID = PwmRequestID.next();
    }

    public RestAuthentication getRestAuthentication( )
    {
        return restAuthentication;
    }

    public PwmApplication getPwmApplication()
    {
        return pwmDomain.getPwmApplication();
    }

    public PwmDomain getDomain( )
    {
        return pwmDomain;
    }

    public Optional<HttpContentType> readContentType( )
    {
        return HttpContentType.fromContentTypeHeader( readHeaderValueAsString( HttpHeader.ContentType ), null );
    }

    public Optional<HttpContentType> readAcceptType( )
    {
        return readAcceptType( getHttpServletRequest() );
    }

    static Optional<HttpContentType> readAcceptType( final HttpServletRequest request )
    {
        final String acceptHeaderValue = request.getHeader( HttpHeader.Accept.getHttpName() );
        return HttpContentType.fromContentTypeHeader( acceptHeaderValue, HttpContentType.json );
    }

    public Locale getLocale( )
    {
        final List<Locale> knownLocales = getAppConfig().getKnownLocales();
        return LocaleHelper.localeResolver( getHttpServletRequest().getLocale(), knownLocales );
    }

    public SessionLabel getSessionLabel( )
    {
        return sessionLabel;
    }

    public ChaiProvider getChaiProvider( final ProfileID ldapProfileID )
            throws PwmUnrecoverableException
    {
        if ( getRestAuthentication().getType() == RestAuthenticationType.LDAP )
        {
            if ( !getRestAuthentication().getLdapIdentity().getLdapProfileID().equals( ldapProfileID ) )
            {
                final String errorMsg = "target user ldap profileID does not match authenticated user ldap profileID";
                throw PwmUnrecoverableException.newException( PwmError.ERROR_REST_INVOCATION_ERROR, errorMsg );
            }
            return getRestAuthentication().getChaiProvider();
        }
        return getDomain().getProxyChaiProvider( getSessionLabel(), ldapProfileID );
    }

    public PwmRequestContext getPwmRestRequest()
    {
        return new PwmRequestContext( getPwmApplication(), pwmDomain.getDomainID(), this.getSessionLabel(), this.getLocale(), requestID );
    }
}

