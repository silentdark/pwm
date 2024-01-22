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

package password.pwm.config.profile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import password.pwm.config.PwmSetting;
import password.pwm.config.PwmSettingCategory;
import password.pwm.config.PwmSettingSyntax;

public class ProfileUtilityTest
{
    @Test
    public void test1()
    {
        for ( final PwmSettingCategory pwmSettingCategory : PwmSettingCategory.values() )
        {
            if ( pwmSettingCategory.hasProfiles() )
            {
                final PwmSetting profileSetting = pwmSettingCategory.getProfileSetting().get();
                Assertions.assertEquals( PwmSettingSyntax.PROFILE, profileSetting.getSyntax() );
                Assertions.assertFalse( profileSetting.getCategory().hasProfiles() );
                Assertions.assertEquals( pwmSettingCategory.getScope(), profileSetting.getCategory().getScope() );
            }
        }
    }

    @Test
    public void test2()
    {
        for ( final ProfileDefinition profileDefinition : ProfileDefinition.values() )
        {
            Assertions.assertTrue( profileDefinition.getCategory().hasProfiles() );
            final PwmSetting profileSetting = profileDefinition.getCategory().getProfileSetting().get();
            Assertions.assertEquals( PwmSettingSyntax.PROFILE, profileSetting.getSyntax() );
            Assertions.assertFalse( profileSetting.getCategory().hasProfiles() );
        }
    }
}
