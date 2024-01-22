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

import org.jetbrains.annotations.NotNull;
import password.pwm.PwmConstants;
import password.pwm.bean.DomainID;
import password.pwm.bean.ProfileID;
import password.pwm.config.PwmSetting;
import password.pwm.config.PwmSettingSyntax;
import password.pwm.i18n.Config;
import password.pwm.i18n.PwmLocaleBundle;
import password.pwm.util.i18n.LocaleHelper;
import password.pwm.util.java.PwmUtil;
import password.pwm.util.java.StringUtil;

import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record StoredConfigKey(
        RecordType recordType,
        DomainID domainID,
        String recordID,
        String profileID
)
        implements Comparable<StoredConfigKey>
{
    private static final Comparator<StoredConfigKey> COMPARATOR = makeComparator();

    public enum RecordType
    {
        SETTING( "Setting" ),
        LOCALE_BUNDLE ( "Localization" ),
        PROPERTY ( "Property" ),;

        private final String label;

        RecordType( final String label )
        {
            this.label = label;
        }

        public String getLabel()
        {
            return label;
        }
    }

    public StoredConfigKey
    {
        Objects.requireNonNull( recordType );
        Objects.requireNonNull( recordID );
        Objects.requireNonNull( domainID );
    }

    public RecordType getRecordType()
    {
        return recordType;
    }

    public DomainID getDomainID()
    {
        return domainID;
    }

    public String getRecordID()
    {
        return recordID;
    }

    public Optional<ProfileID> getProfileID()
    {
        if ( !isRecordType( RecordType.SETTING ) )
        {
            throw new IllegalStateException( "can not read profileID for non-setting record type" );
        }
        return ProfileID.createNullable( profileID );
    }

    public String getLocaleKey()
    {
        if ( !isRecordType( RecordType.LOCALE_BUNDLE ) )
        {
            throw new IllegalStateException( "can not read profileID for non-locale record type" );
        }
        return profileID;
    }

    public static StoredConfigKey forSetting( final PwmSetting pwmSetting, final ProfileID profileID, final DomainID domainID )
    {
        return new StoredConfigKey( RecordType.SETTING, domainID, pwmSetting.getKey(), profileID == null ? null : profileID.stringValue() );
    }

    public static StoredConfigKey forLocaleBundle( final PwmLocaleBundle localeBundle, final String key, final DomainID domainID )
    {
        return new StoredConfigKey( RecordType.LOCALE_BUNDLE, domainID, localeBundle.getKey(), key );
    }

    static StoredConfigKey forConfigurationProperty( final ConfigurationProperty configurationProperty )
    {
        return new StoredConfigKey( RecordType.PROPERTY, DomainID.systemId(), configurationProperty.getKey(), null );
    }

    public StoredConfigKey withNewDomain( final DomainID domainID )
    {
        return new StoredConfigKey( this.recordType, domainID, this.recordID, this.profileID );
    }

    public boolean isRecordType( final RecordType recordType )
    {
        return Objects.equals( this.recordType, recordType );
    }

    public boolean isValid()
    {
        try
        {
            validate();
            return true;
        }
        catch ( final IllegalStateException e )
        {
            /* ignore */
        }
        return false;
    }

    public void validate()
    {
        switch ( recordType )
        {
            case SETTING:
            {
                final PwmSetting pwmSetting = this.toPwmSetting();
                final boolean hasProfileID = StringUtil.notEmpty( profileID );
                if ( pwmSetting.getCategory().hasProfiles() && !hasProfileID )
                {
                    throw new IllegalStateException( "profileID is required for setting " + pwmSetting.getKey() );
                }
                else if ( !pwmSetting.getCategory().hasProfiles() && hasProfileID )
                {
                    throw new IllegalStateException( "profileID is not required for setting " + pwmSetting.getKey() );
                }
            }
            break;

            case LOCALE_BUNDLE:
            {
                Objects.requireNonNull( profileID, "profileID is required when recordType is LOCALE_BUNDLE" );
                final PwmLocaleBundle pwmLocaleBundle = toLocaleBundle();
                if ( !pwmLocaleBundle.getDisplayKeys().contains( profileID ) )
                {
                    throw new IllegalStateException( "key '" + profileID + "' is unrecognized for locale bundle " + pwmLocaleBundle.name() );
                }
            }
            break;

            case PROPERTY:
                break;

            default:
                PwmUtil.unhandledSwitchStatement( recordType );
        }
    }

    public String getLabel( final Locale locale )
    {
        final String separator = LocaleHelper.getLocalizedMessage( locale, Config.Display_SettingNavigationSeparator, null );

        final String prefix = "[Domain: " + domainID + "]" + separator + recordType.getLabel() + separator;

        switch ( recordType )
        {
            case SETTING:
                if ( toPwmSetting().getCategory().hasProfiles()  )
                {
                    return prefix + toPwmSetting().toMenuLocationDebug( getProfileID().orElse( null ), locale );
                }
                else if ( StringUtil.notEmpty( profileID ) )
                {
                    return prefix + toPwmSetting().toMenuLocationDebug( null, locale ) + separator + "[profile: " + profileID + "]";
                }
                return prefix + toPwmSetting().toMenuLocationDebug( null, locale );
            case PROPERTY:
                return prefix + this.getRecordID();

            case LOCALE_BUNDLE:
                return prefix
                        + this.getRecordID()
                        + separator
                        + this.getProfileID();

            default:
                PwmUtil.unhandledSwitchStatement( recordType );
        }

        throw new IllegalStateException(  );
    }

    public PwmSetting toPwmSetting()
    {
        if ( getRecordType() != RecordType.SETTING )
        {
            throw new IllegalStateException( "attempt to read pwmSetting key for non-setting ConfigItemKey" );
        }

        return PwmSetting.forKey( this.recordID ).orElseThrow( () -> new IllegalStateException(
                "attempt to read ConfigItemKey with unknown setting key '" + getRecordID() + "'" ) );
    }

    public PwmLocaleBundle toLocaleBundle()
    {
        if ( getRecordType() != RecordType.LOCALE_BUNDLE )
        {
            throw new IllegalStateException( "attempt to read PwmLocaleBundle key for non-locale ConfigItemKey" );
        }

        return PwmLocaleBundle.forKey( this.recordID )
                .orElseThrow( () -> new IllegalStateException( "unexpected key value for locale bundle: " + this.recordID ) );
    }

    public ConfigurationProperty toConfigurationProperty()
    {
        if ( getRecordType() != RecordType.PROPERTY )
        {
            throw new IllegalStateException( "attempt to read ConfigurationProperty key for non-config property ConfigItemKey" );
        }

        return ConfigurationProperty.valueOf( recordID );
    }

    @Override
    public int compareTo( @NotNull final StoredConfigKey o )
    {
        return COMPARATOR.compare( this, o );
    }

    @Override
    public String toString()
    {
        return getLabel( PwmConstants.DEFAULT_LOCALE );
    }

    public static Set<DomainID> uniqueDomains( final Set<StoredConfigKey> storedConfigKeys )
    {
        return storedConfigKeys.stream()
                .map( StoredConfigKey::getDomainID )
                .collect( Collectors.toUnmodifiableSet() );
    }

    public PwmSettingSyntax getSyntax()
    {
        switch ( getRecordType() )
        {
            case SETTING:
                return toPwmSetting().getSyntax();

            case PROPERTY:
                return PwmSettingSyntax.STRING;

            case LOCALE_BUNDLE:
                return PwmSettingSyntax.LOCALIZED_STRING_ARRAY;

            default:
                PwmUtil.unhandledSwitchStatement( getRecordType() );
                throw new IllegalStateException();
        }
    }

    public static Comparator<StoredConfigKey> comparator()
    {
        return COMPARATOR;
    }

    private static Comparator<StoredConfigKey> makeComparator()
    {
        final Comparator<StoredConfigKey> typeComparator = Comparator.comparing(
                StoredConfigKey::getRecordType,
                Comparator.nullsLast( Comparator.naturalOrder() ) );


        final Comparator<StoredConfigKey> recordComparator = ( o1, o2 ) ->
        {
            if ( Objects.equals( o1.getRecordType(), o2.getRecordType() )
                    && o1.isRecordType( RecordType.SETTING ) )
            {
                final Comparator<PwmSetting> pwmSettingComparator = PwmSetting.menuLocationComparator( );
                return pwmSettingComparator.compare( o1.toPwmSetting(), o2.toPwmSetting() );
            }
            else
            {
                return o1.getRecordID().compareTo( o2.getRecordID() );
            }
        };

        return Comparator.comparing( StoredConfigKey::getDomainID, DomainID.comparator() )
                .thenComparing( typeComparator )
                .thenComparing( recordComparator )
                .thenComparing( key -> key.profileID, ProfileID.stringComparator() );
    }

}
