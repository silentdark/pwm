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

import org.junit.Assert;
import org.junit.Test;
import password.pwm.PwmConstants;
import password.pwm.config.Configuration;
import password.pwm.config.stored.StoredConfigurationFactory;
import password.pwm.error.PwmUnrecoverableException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class HealthMessageTest
{

    @Test
    public void testHealthMessageUniqueKeys()
    {
        final Set<String> seenKeys = new HashSet<>();
        for ( final HealthMessage healthMessage : HealthMessage.values() )
        {
            // duplicate key found
            Assert.assertFalse( seenKeys.contains( healthMessage.getKey() ) );
            seenKeys.add( healthMessage.getKey() );
        }
    }

    @Test
    public void testHealthMessageDescription() throws PwmUnrecoverableException
    {
        final Configuration configuration = new Configuration( StoredConfigurationFactory.newConfig() );
        final Locale locale = PwmConstants.DEFAULT_LOCALE;
        for ( final HealthMessage healthMessage : HealthMessage.values() )
        {
            healthMessage.getDescription( locale, configuration, new String[] {"field1", "field2"} );
        }
    }
}
