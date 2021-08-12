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

package password.pwm.config.stored;

import org.junit.Assert;
import org.junit.Test;
import password.pwm.config.PwmSetting;
import password.pwm.config.value.StringValue;
import password.pwm.error.PwmUnrecoverableException;

import java.util.Set;

public class StoredConfigurationUtilTest
{

    @Test
    public void testChangedValues() throws PwmUnrecoverableException
    {
        final StoredConfiguration storedConfiguration = StoredConfigurationFactory.newConfig();
        final StoredConfigurationModifier modifier = StoredConfigurationModifier.newModifier( storedConfiguration );

        modifier.writeSetting( PwmSetting.NOTES, null, new StringValue( "notes test" ), null );

        final StoredConfiguration newConfig = modifier.newStoredConfiguration();

        final Set<StoredConfigItemKey> modifiedKeys = StoredConfigurationUtil.changedValues( storedConfiguration, newConfig );
        Assert.assertEquals( modifiedKeys.size(), 1 );
        Assert.assertEquals( modifiedKeys.iterator().next(), StoredConfigItemKey.fromSetting( PwmSetting.NOTES, null ) );


        final StoredConfigurationModifier modifier2 = StoredConfigurationModifier.newModifier( newConfig );
        modifier2.resetSetting( PwmSetting.NOTES, null, null );
        final StoredConfiguration resetConfig = modifier2.newStoredConfiguration();
        final Set<StoredConfigItemKey> resetKeys = StoredConfigurationUtil.changedValues( newConfig, resetConfig );
        Assert.assertEquals( resetKeys.size(), 1 );
        Assert.assertEquals( resetKeys.iterator().next(), StoredConfigItemKey.fromSetting( PwmSetting.NOTES, null ) );

    }
}
