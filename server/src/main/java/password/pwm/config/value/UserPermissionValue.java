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

import com.google.gson.reflect.TypeToken;
import password.pwm.config.PwmSetting;
import password.pwm.config.stored.StoredConfigXmlSerializer;
import password.pwm.config.stored.XmlOutputProcessData;
import password.pwm.config.value.data.UserPermission;
import password.pwm.error.PwmOperationalException;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.ldap.permission.UserPermissionUtility;
import password.pwm.ldap.permission.UserPermissionType;
import password.pwm.util.java.JsonUtil;
import password.pwm.util.java.XmlElement;
import password.pwm.util.java.XmlFactory;
import password.pwm.util.secure.PwmSecurityKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class UserPermissionValue extends AbstractValue implements StoredValue
{
    private final List<UserPermission> values;

    private boolean needsXmlUpdate;

    public UserPermissionValue( final List<UserPermission> values )
    {
       this.values = sanitizeList( values );
    }

    private List<UserPermission> sanitizeList( final List<UserPermission> permissions )
    {
        final List<UserPermission> tempList = new ArrayList<>();
        if ( permissions != null )
        {
            tempList.addAll( permissions );
        }

        while ( tempList.contains( null ) )
        {
            tempList.remove( null );
        }

        Collections.sort( tempList );
        return Collections.unmodifiableList( tempList );
    }

    public static StoredValueFactory factory( )
    {
        return new StoredValueFactory()
        {
            @Override
            public UserPermissionValue fromJson( final String input )
            {
                if ( input == null )
                {
                    return new UserPermissionValue( Collections.emptyList() );
                }
                else
                {
                    List<UserPermission> srcList = JsonUtil.deserialize( input, new TypeToken<List<UserPermission>>()
                    {
                    } );
                    srcList = srcList == null ? Collections.emptyList() : srcList;
                    return new UserPermissionValue( Collections.unmodifiableList( srcList ) );
                }
            }

            @Override
            public UserPermissionValue fromXmlElement( final PwmSetting pwmSetting, final XmlElement settingElement, final PwmSecurityKey key )
                    throws PwmOperationalException
            {
                final boolean newType = "2".equals(
                        settingElement.getAttributeValue( StoredConfigXmlSerializer.StoredConfigXmlConstants.XML_ATTRIBUTE_SYNTAX_VERSION ) );
                final List<XmlElement> valueElements = settingElement.getChildren( "value" );
                final List<UserPermission> values = new ArrayList<>();
                for ( final XmlElement loopValueElement : valueElements )
                {
                    final String value = loopValueElement.getText();
                    if ( value != null && !value.isEmpty() )
                    {
                        if ( newType )
                        {
                            final UserPermission userPermission = JsonUtil.deserialize( value, UserPermission.class );
                            values.add( userPermission );
                        }
                        else
                        {
                            values.add( UserPermission.builder()
                                    .type( UserPermissionType.ldapQuery )
                                    .ldapQuery( value )
                                    .build() );
                        }
                    }
                }
                final UserPermissionValue userPermissionValue = new UserPermissionValue( values );
                userPermissionValue.needsXmlUpdate = !newType;
                return userPermissionValue;
            }
        };
    }

    @Override
    public List<XmlElement> toXmlValues( final String valueElementName, final XmlOutputProcessData xmlOutputProcessData )
    {
        final List<XmlElement> returnList = new ArrayList<>();
        for ( final UserPermission value : values )
        {
            final XmlElement valueElement = XmlFactory.getFactory().newElement( valueElementName );
            valueElement.addText( JsonUtil.serialize( value ) );
            returnList.add( valueElement );
        }
        return returnList;
    }

    @Override
    public List<UserPermission> toNativeObject( )
    {
        return Collections.unmodifiableList( values );
    }

    @Override
    public List<String> validateValue( final PwmSetting pwmSetting )
    {
        final List<String> returnObj = new ArrayList<>();
        for ( final UserPermission userPermission : values )
        {
            try
            {
                 UserPermissionUtility.validatePermissionSyntax( userPermission );
            }
            catch ( final PwmUnrecoverableException e )
            {
                returnObj.add( e.getMessage() );
            }
        }
        return returnObj;
    }

    public boolean isNeedsXmlUpdate( )
    {
        return needsXmlUpdate;
    }

    @Override
    public int currentSyntaxVersion( )
    {
        return 2;
    }

    @Override
    public String toDebugString( final Locale locale )
    {
        if ( values != null && !values.isEmpty() )
        {
            final StringBuilder sb = new StringBuilder();
            for ( final Iterator<UserPermission> iterator = values.iterator(); iterator.hasNext(); )
            {
                final UserPermission userPermission = iterator.next();
                sb.append( "UserPermission: " );
                sb.append( userPermission.debugString() );
                if ( iterator.hasNext() )
                {
                    sb.append( "\n" );
                }
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }
}
