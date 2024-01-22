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

package password.pwm.util.localdb;

import password.pwm.PwmApplication;
import password.pwm.PwmApplicationMode;
import password.pwm.PwmEnvironment;
import password.pwm.bean.DomainID;
import password.pwm.config.AppConfig;
import password.pwm.config.PwmSetting;
import password.pwm.config.stored.StoredConfigKey;
import password.pwm.config.stored.StoredConfiguration;
import password.pwm.config.stored.StoredConfigurationFactory;
import password.pwm.config.stored.StoredConfigurationModifier;
import password.pwm.config.value.StringValue;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.logging.PwmLogLevel;

import java.nio.file.Path;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class TestHelper
{
    public static PwmApplication makeTestPwmApplication( final Path tempFolder )
            throws PwmUnrecoverableException
    {
        final StoredConfiguration storedConfiguration = StoredConfigurationFactory.newConfig();
        final StoredConfigurationModifier modifier = StoredConfigurationModifier.newModifier( storedConfiguration );
        final StoredConfigKey key = StoredConfigKey.forSetting( PwmSetting.EVENTS_JAVA_STDOUT_LEVEL, null, DomainID.systemId() );
        modifier.writeSetting( key, new StringValue( PwmLogLevel.FATAL.toString() ), null );
        final AppConfig appConfig = AppConfig.forStoredConfig( modifier.newStoredConfiguration() );
        return makeTestPwmApplication( tempFolder, appConfig );
    }

    public static PwmApplication makeTestPwmApplication( final Path tempFolder, final AppConfig appConfig )
            throws PwmUnrecoverableException
    {
        final PwmEnvironment pwmEnvironment = PwmEnvironment.builder()
                .config( appConfig )
                .applicationPath( tempFolder )
                .applicationMode( PwmApplicationMode.READ_ONLY )
                .internalRuntimeInstance( true )
                .build();

        return PwmApplication.createPwmApplication( pwmEnvironment );
    }

    public static <C, R> void testAttributeUniqueness(
            final Collection<C> collection,
            final Function<C, Collection<R>> attributeExtractor,
            final String attributeDebugLabel
    )
    {
        final Set<R> seenAttributes = new HashSet<>();
        for ( final C item : collection )
        {
            final Collection<R> attributes = attributeExtractor.apply( item );
            for ( final R attribute : attributes )
            {
                if ( seenAttributes.contains( attribute ) )
                {
                    throw new IllegalStateException( "item " + item
                            + " contains duplicate " + attributeDebugLabel + " value "
                            + attribute );
                }
                seenAttributes.add( attribute );
            }
        }
    }

    public static <E extends Enum<E>, R> void testEnumAttributeUniqueness(
            final Class<E> enumClass,
            final Function<E, Collection<R>> attributeExtractor,
            final String attributeDebugLabel
    )
    {
        testAttributeUniqueness( EnumSet.allOf( enumClass ), attributeExtractor, attributeDebugLabel );
    }
}
