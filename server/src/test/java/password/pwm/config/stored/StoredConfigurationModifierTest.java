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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import password.pwm.bean.DomainID;
import password.pwm.bean.ProfileID;
import password.pwm.config.PwmSetting;
import password.pwm.config.value.NumericValue;
import password.pwm.config.value.StringValue;
import password.pwm.config.value.ValueTypeConverter;
import password.pwm.error.PwmUnrecoverableException;

import java.util.List;

public class StoredConfigurationModifierTest
{

    @Test
    public void testWriteSetting() throws PwmUnrecoverableException
    {
        final DomainID domainID = DomainID.DOMAIN_ID_DEFAULT;
        final StoredConfigKey key = StoredConfigKey.forSetting( PwmSetting.NOTES, null, domainID );


        final StoredConfiguration storedConfiguration = StoredConfigurationFactory.newConfig();
        final StoredConfigurationModifier modifier = StoredConfigurationModifier.newModifier( storedConfiguration );

        modifier.writeSetting( key, new StringValue( "notes test" ), null );

        final StoredConfiguration newConfig = modifier.newStoredConfiguration();

        final String notesText = ValueTypeConverter.valueToString( newConfig.readStoredValue( key ).orElseThrow() );
        Assertions.assertEquals( "notes test", notesText );
    }

    @Test
    public void testCopyProfileID() throws PwmUnrecoverableException
    {
        final ProfileID newProfileID = ProfileID.create( "newProfile" );

        final DomainID domainID = DomainID.DOMAIN_ID_DEFAULT;
        final StoredConfiguration storedConfiguration = StoredConfigurationFactory.newConfig();
        final StoredConfigurationModifier modifier = StoredConfigurationModifier.newModifier( storedConfiguration );

        final StoredConfigKey key = StoredConfigKey.forSetting( PwmSetting.HELPDESK_RESULT_LIMIT, ProfileID.PROFILE_ID_DEFAULT, domainID );

        modifier.writeSetting( key, new NumericValue( 19 ), null );
        final StoredConfiguration preCopyConfig = modifier.newStoredConfiguration();

        final StoredConfiguration postCopyConfig = StoredConfigurationUtil.copyProfileID(
                preCopyConfig,
                domainID,
                PwmSetting.HELPDESK_RESULT_LIMIT.getCategory(),
                ProfileID.PROFILE_ID_DEFAULT,
                newProfileID,
                null );

        final List<ProfileID> profileNames = StoredConfigurationUtil.profilesForSetting( domainID, PwmSetting.HELPDESK_RESULT_LIMIT, postCopyConfig );
        Assertions.assertEquals( 2, profileNames.size() );
        Assertions.assertTrue( profileNames.contains( ProfileID.PROFILE_ID_DEFAULT ) );
        Assertions.assertTrue( profileNames.contains( newProfileID ) );

        final long copiedResultLimit = ValueTypeConverter.valueToLong( postCopyConfig.readStoredValue( key ).orElseThrow() );
        Assertions.assertEquals( 19, copiedResultLimit );
    }
}
