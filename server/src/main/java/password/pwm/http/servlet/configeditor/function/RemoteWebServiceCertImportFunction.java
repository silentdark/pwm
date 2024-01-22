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

import password.pwm.bean.UserIdentity;
import password.pwm.config.stored.StoredConfigKey;
import password.pwm.config.stored.StoredConfigurationModifier;
import password.pwm.config.stored.StoredConfigurationUtil;
import password.pwm.config.value.RemoteWebServiceValue;
import password.pwm.config.value.data.RemoteWebServiceConfiguration;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class RemoteWebServiceCertImportFunction extends AbstractUriCertImportFunction
{

    @Override
    String getUri( final StoredConfigurationModifier modifier, final StoredConfigKey key, final String extraData )
            throws PwmOperationalException, PwmUnrecoverableException
    {
        final RemoteWebServiceValue actionValue = ( RemoteWebServiceValue ) StoredConfigurationUtil.getValueOrDefault( modifier.newStoredConfiguration(), key );
        final String serviceName = actionNameFromExtraData( extraData );
        final RemoteWebServiceConfiguration action = actionValue.forName( serviceName );
        final String uriString = action.getUrl();

        return validateUriStringSetting( uriString, key );
    }

    private String actionNameFromExtraData( final String extraData )
    {
        return extraData;
    }

    @Override
    void store(
            final List<X509Certificate> certs,
            final StoredConfigurationModifier modifier,
            final StoredConfigKey key,
            final String extraData,
            final UserIdentity userIdentity
    )
            throws PwmOperationalException, PwmUnrecoverableException
    {
        final RemoteWebServiceValue actionValue = ( RemoteWebServiceValue )  StoredConfigurationUtil.getValueOrDefault( modifier.newStoredConfiguration(), key );
        final String actionName = actionNameFromExtraData( extraData );
        final List<RemoteWebServiceConfiguration> newList = new ArrayList<>();
        for ( final RemoteWebServiceConfiguration loopConfiguration : actionValue.toNativeObject() )
        {
            if ( actionName.equals( loopConfiguration.getName() ) )
            {
                final RemoteWebServiceConfiguration newConfig = loopConfiguration.toBuilder()
                        .certificates( certs )
                        .build();
                newList.add( newConfig );
            }
            else
            {
                newList.add( loopConfiguration );
            }
        }
        final RemoteWebServiceValue newActionValue = new RemoteWebServiceValue( newList );
        modifier.writeSetting( key, newActionValue, userIdentity );
    }

}
