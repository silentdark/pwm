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

package password.pwm.config.value;

import password.pwm.PwmConstants;
import password.pwm.config.PwmSetting;
import password.pwm.config.stored.StoredConfigXmlSerializer;
import password.pwm.config.stored.XmlOutputProcessData;
import password.pwm.i18n.Display;
import password.pwm.util.java.JsonUtil;
import password.pwm.util.java.XmlElement;
import password.pwm.util.java.XmlFactory;
import password.pwm.util.secure.PwmSecurityKey;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class BooleanValue implements StoredValue
{
    private static final BooleanValue POSITIVE = new BooleanValue( true );
    private static final BooleanValue NEGATIVE = new BooleanValue( false );

    private final boolean value;

    private BooleanValue( final boolean value )
    {
        this.value = value;
    }

    public static BooleanValue of( final boolean input )
    {
        return input ? POSITIVE : NEGATIVE;
    }

    public static StoredValueFactory factory( )
    {
        return new StoredValueFactory()
        {
            @Override
            public BooleanValue fromJson( final String value )
            {
                return BooleanValue.of( JsonUtil.deserialize( value, Boolean.class ) );
            }

            @Override
            public BooleanValue fromXmlElement( final PwmSetting pwmSetting, final XmlElement settingElement, final PwmSecurityKey input )
            {
                final Optional<XmlElement> valueElement = settingElement.getChild( StoredConfigXmlSerializer.StoredConfigXmlConstants.XML_ELEMENT_VALUE );
                if ( valueElement.isPresent() )
                {
                    final String value = valueElement.get().getTextTrim();
                    return BooleanValue.of( Boolean.parseBoolean( value ) );
                }
                return BooleanValue.of( false );
            }

        };
    }

    @Override
    public List<String> validateValue( final PwmSetting pwmSetting )
    {
        return Collections.emptyList();
    }

    @Override
    public List<XmlElement> toXmlValues( final String valueElementName, final XmlOutputProcessData xmlOutputProcessData )
    {
        final XmlElement valueElement = XmlFactory.getFactory().newElement( valueElementName );
        valueElement.addText( String.valueOf( value ) );
        return Collections.singletonList( valueElement );
    }

    @Override
    public Object toNativeObject( )
    {
        return value;
    }

    @Override
    public String toDebugString( final Locale locale )
    {
        final Locale loc = ( locale == null )
                ? PwmConstants.DEFAULT_LOCALE
                : locale;
        return value
                ? Display.getLocalizedMessage( loc, Display.Value_True, null )
                : Display.getLocalizedMessage( loc, Display.Value_False, null );
    }

    @Override
    public Serializable toDebugJsonObject( final Locale locale )
    {
        return value;
    }

    @Override
    public int currentSyntaxVersion( )
    {
        return 0;
    }

    @Override
    public String valueHash()
    {
        return value ? "1" : "0";
    }
}
