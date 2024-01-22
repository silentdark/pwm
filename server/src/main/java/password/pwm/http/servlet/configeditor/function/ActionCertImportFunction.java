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
import password.pwm.config.value.ActionValue;
import password.pwm.config.value.StoredValue;
import password.pwm.config.value.ValueTypeConverter;
import password.pwm.config.value.data.ActionConfiguration;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.json.JsonFactory;

import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class ActionCertImportFunction extends AbstractUriCertImportFunction
{
    private static final String KEY_ITERATION = "iteration";
    private static final String KEY_WEB_ACTION_ITERATION = "webActionIter";

    @Override
    String getUri(
            final StoredConfigurationModifier modifier,
            final StoredConfigKey key,
            final String extraData
    )
            throws PwmOperationalException, PwmUnrecoverableException
    {
        final Map<String, Integer> extraDataMap = JsonFactory.get().deserializeMap( extraData, String.class, Integer.class );

        final StoredValue actionValue = StoredConfigurationUtil.getValueOrDefault( modifier.newStoredConfiguration(), key );
        final List<ActionConfiguration> actionConfigurations = ValueTypeConverter.valueToAction( key.toPwmSetting(), actionValue );
        final ActionConfiguration action = actionConfigurations.get( extraDataMap.get( KEY_ITERATION ) );
        final ActionConfiguration.WebAction webAction = action.getWebActions().get( extraDataMap.get( KEY_WEB_ACTION_ITERATION ) );

        final String uriString = webAction.getUrl();

        return validateUriStringSetting( uriString, key );
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
        final Map<String, Integer> extraDataMap = JsonFactory.get().deserializeMap( extraData, String.class, Integer.class );

        final StoredValue actionValue = StoredConfigurationUtil.getValueOrDefault( modifier.newStoredConfiguration(), key );
        final List<ActionConfiguration> actionConfigurations = ValueTypeConverter.valueToAction( key.toPwmSetting(), actionValue );
        final ActionConfiguration action = actionConfigurations.get( extraDataMap.get( KEY_ITERATION ) );
        final ActionConfiguration.WebAction webAction = action.getWebActions().get( extraDataMap.get( KEY_WEB_ACTION_ITERATION ) );

        final ActionConfiguration.WebAction clonedAction = webAction.toBuilder()
                .certificates( certs )
                .build();

        action.getWebActions().set( extraDataMap.get( KEY_WEB_ACTION_ITERATION ), clonedAction );

        final ActionValue newActionValue = new ActionValue( actionConfigurations );
        modifier.writeSetting( key, newActionValue, userIdentity );
    }
}
