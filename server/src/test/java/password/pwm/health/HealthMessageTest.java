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

package password.pwm.health;

import org.junit.jupiter.api.Test;
import password.pwm.PwmConstants;
import password.pwm.config.AppConfig;
import password.pwm.config.DomainConfig;
import password.pwm.config.stored.StoredConfigurationFactory;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.localdb.TestHelper;

import java.util.List;
import java.util.Locale;

public class HealthMessageTest
{

    @Test
    public void testHealthMessageUniqueKeys()
    {
        TestHelper.testEnumAttributeUniqueness(
                HealthMessage.class,
                healthMessage -> List.of( healthMessage.getKey() ),
                "key" );
    }

    @Test
    public void testHealthMessageDescription() throws PwmUnrecoverableException
    {
        final AppConfig appConfig = AppConfig.forStoredConfig( StoredConfigurationFactory.newConfig() );
        final Locale locale = PwmConstants.DEFAULT_LOCALE;
        for ( final DomainConfig domainConfig : appConfig.getDomainConfigs().values() )
        {
            for ( final HealthMessage healthMessage : HealthMessage.values() )
            {
                healthMessage.getDescription( locale, domainConfig, new String[]
                        {
                                "field1",
                                "field2",
                        } );
            }
        }
    }
}
