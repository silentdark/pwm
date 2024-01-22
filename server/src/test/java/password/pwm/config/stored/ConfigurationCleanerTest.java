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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import password.pwm.bean.DomainID;
import password.pwm.bean.ProfileID;
import password.pwm.config.AppConfig;
import password.pwm.config.DomainConfig;
import password.pwm.config.PwmSetting;
import password.pwm.config.option.ADPolicyComplexity;
import password.pwm.config.option.RecoveryMinLifetimeOption;
import password.pwm.config.option.WebServiceUsage;
import password.pwm.config.profile.ForgottenPasswordProfile;
import password.pwm.config.profile.PeopleSearchProfile;
import password.pwm.config.profile.PwmPasswordPolicy;
import password.pwm.config.value.data.UserPermission;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public class ConfigurationCleanerTest
{
    private static DomainConfig domainConfig;
    private static final DomainID DOMAIN_ID = DomainID.create( "default" );

    @BeforeAll
    public static void setUp() throws Exception
    {
        //PwmLogger.disableAllLogging();

        try ( InputStream xmlFile = ConfigurationCleanerTest.class.getResourceAsStream( "ConfigurationCleanerTest.xml" ) )
        {
            final StoredConfiguration storedConfiguration = StoredConfigurationFactory.input( xmlFile );
            domainConfig = AppConfig.forStoredConfig( storedConfiguration ).getDomainConfigs().get( DOMAIN_ID );
        }
    }

    @AfterAll
    public static void tearDown()
    {
        domainConfig = null;
    }

    @Test
    public void testCleaningConfigFileLoaded()
    {
        final String notesText = domainConfig.readSettingAsString( PwmSetting.NOTES );
        Assertions.assertEquals( "deprecated-test-configuration-file", notesText );
    }

    @Test
    public void testProfiledSettings()
    {
        final List<ProfileID> profileList = StoredConfigurationUtil.profilesForSetting(
                DOMAIN_ID, PwmSetting.PEOPLE_SEARCH_PHOTO_QUERY_FILTER, domainConfig.getStoredConfiguration() );
        Assertions.assertEquals( 1, profileList.size() );

        final PeopleSearchProfile peopleSearchProfile = domainConfig.getPeopleSearchProfiles().get( ProfileID.PROFILE_ID_DEFAULT );
        final List<UserPermission> userPermissionList = peopleSearchProfile.readSettingAsUserPermission( PwmSetting.PEOPLE_SEARCH_PHOTO_QUERY_FILTER );
        final UserPermission userPermission = userPermissionList.iterator().next();
        Assertions.assertEquals( "(|(cn=*smith*)(cn=*blake*)(givenName=*Margo*))", userPermission.getLdapQuery() );
    }

    @Test
    public void testDeprecatedPublicHealthStatsWebService()
    {
        {
            final Set<WebServiceUsage> usages = domainConfig.readSettingAsOptionList( PwmSetting.WEBSERVICES_PUBLIC_ENABLE, WebServiceUsage.class );
            Assertions.assertEquals( 2, usages.size() );
            Assertions.assertTrue( usages.contains( WebServiceUsage.Statistics ) );
            Assertions.assertTrue( usages.contains( WebServiceUsage.Health ) );
        }
    }

    @Test
    public void testDeprecatedMinLifetimeSetting()
    {
        for ( final ForgottenPasswordProfile profile : domainConfig.getForgottenPasswordProfiles().values() )
        {
            final RecoveryMinLifetimeOption minLifetimeOption = profile.readSettingAsEnum(
                    PwmSetting.RECOVERY_MINIMUM_PASSWORD_LIFETIME_OPTIONS,
                    RecoveryMinLifetimeOption.class
            );
            Assertions.assertEquals( RecoveryMinLifetimeOption.NONE, minLifetimeOption );
        }
    }

    @Test
    public void testDeprecatedAdComplexitySettings()
    {
        for ( final ProfileID profile : domainConfig.getPasswordProfileIDs() )
        {
            final PwmPasswordPolicy pwmPasswordPolicy = domainConfig.getPasswordPolicy( profile );
            final ADPolicyComplexity adPolicyComplexity = pwmPasswordPolicy.ruleHelper().getADComplexityLevel();

            Assertions.assertEquals( ADPolicyComplexity.AD2003, adPolicyComplexity );
        }
    }
}
