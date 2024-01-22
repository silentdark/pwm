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

package password.pwm.http.servlet.configeditor.function;

import password.pwm.PwmDomain;
import password.pwm.bean.UserIdentity;
import password.pwm.config.AppConfig;
import password.pwm.config.PwmSetting;
import password.pwm.config.stored.StoredConfigKey;
import password.pwm.config.stored.StoredConfigurationModifier;
import password.pwm.config.value.ValueTypeConverter;
import password.pwm.config.value.X509CertificateValue;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmError;
import password.pwm.error.PwmException;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.http.PwmRequest;
import password.pwm.http.PwmSession;
import password.pwm.i18n.Message;
import password.pwm.svc.event.SyslogAuditService;
import password.pwm.util.java.CollectionUtil;
import password.pwm.util.secure.X509Utils;

import java.security.cert.X509Certificate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SyslogCertImportFunction implements SettingUIFunction
{

    @Override
    public String provideFunction(
            final PwmRequest pwmRequest,
            final StoredConfigurationModifier modifier,
            final StoredConfigKey key,
            final String extraData
    )
            throws PwmOperationalException, PwmUnrecoverableException
    {
        boolean error = false;
        Exception exeception = null;
        final PwmDomain pwmDomain = pwmRequest.getPwmDomain();
        final PwmSession pwmSession = pwmRequest.getPwmSession();

        final Set<X509Certificate> resultCertificates = new LinkedHashSet<>();

        final var syslogServerSetting = StoredConfigKey.forSetting( PwmSetting.AUDIT_SYSLOG_SERVERS, key.getProfileID().orElse( null ), key.getDomainID() );
        final List<String> syslogConfigStrs = ValueTypeConverter.valueToStringArray( modifier.newStoredConfiguration().readStoredValue( syslogServerSetting ).orElseThrow() );
        if ( !CollectionUtil.isEmpty( syslogConfigStrs ) )
        {
            for ( final String entry : syslogConfigStrs )
            {
                if ( entry.toUpperCase().startsWith( "TLS" ) )
                {
                    final SyslogAuditService.SyslogConfig syslogConfig = SyslogAuditService.SyslogConfig.fromConfigString( entry );
                    if ( syslogConfig != null )
                    {
                        try
                        {
                            final List<X509Certificate> certs = X509Utils.readRemoteCertificates(
                                    syslogConfig.getHost(),
                                    syslogConfig.getPort(),
                                    AppConfig.defaultConfig() );
                            if ( certs != null )
                            {
                                resultCertificates.addAll( certs );
                                error = false;
                            }
                        }
                        catch ( final Exception e )
                        {
                            error = true;
                            exeception = e;
                        }
                    }
                }
            }
        }

        if ( !error )
        {
            final UserIdentity userIdentity = pwmSession.isAuthenticated() ? pwmSession.getUserInfo().getUserIdentity() : null;
            modifier.writeSetting( key, X509CertificateValue.fromX509( resultCertificates ), userIdentity );
            return Message.getLocalizedMessage( pwmSession.getSessionStateBean().getLocale(), Message.Success_Unknown, pwmDomain.getConfig() );
        }
        else
        {
            if ( exeception instanceof PwmException )
            {
                throw new PwmOperationalException( ( ( PwmException ) exeception ).getErrorInformation() );
            }
            final ErrorInformation errorInformation = new ErrorInformation( PwmError.ERROR_INTERNAL, "error importing certificates: " + exeception.getMessage() );
            throw new PwmOperationalException( errorInformation );
        }
    }
}
