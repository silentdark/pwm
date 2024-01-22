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

package password.pwm.svc.node;

import com.novell.ldapchai.ChaiUser;
import com.novell.ldapchai.exception.ChaiException;
import lombok.Value;
import password.pwm.PwmDomain;
import password.pwm.bean.ProfileID;
import password.pwm.bean.UserIdentity;
import password.pwm.config.PwmSetting;
import password.pwm.config.profile.LdapProfile;
import password.pwm.error.ErrorInformation;
import password.pwm.error.PwmError;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.java.LazySupplier;
import password.pwm.util.json.JsonFactory;
import password.pwm.util.java.StringUtil;
import password.pwm.util.java.TimeDuration;
import password.pwm.util.logging.PwmLogger;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

class LDAPNodeDataService implements NodeDataServiceProvider
{
    private static final PwmLogger LOGGER = PwmLogger.forClass( LDAPNodeDataService.class );

    private final PwmDomain pwmDomain;
    private static final String VALUE_PREFIX = "0006#.#.#";
    private final NodeService nodeService;
    private final LazySupplier.CheckedSupplier<LDAPHelper, PwmUnrecoverableException> ldapHelperSupplier;

    LDAPNodeDataService( final NodeService nodeService, final PwmDomain pwmDomain )
            throws PwmUnrecoverableException
    {
        this.nodeService = nodeService;
        this.pwmDomain = Objects.requireNonNull( pwmDomain );

        final LdapProfile ldapProfile = pwmDomain.getConfig().getDefaultLdapProfile();
        final String testUser = ldapProfile.readSettingAsString( PwmSetting.LDAP_TEST_USER_DN );

        if ( StringUtil.isEmpty( testUser ) )
        {
            final String msg = "ldap node service requires that setting "
                    + PwmSetting.LDAP_TEST_USER_DN.toMenuLocationDebug( ldapProfile.getId(), null )
                    + " is configured";
            throw PwmUnrecoverableException.newException( PwmError.ERROR_NODE_SERVICE_ERROR, msg );
        }

        ldapHelperSupplier = LazySupplier.checked( () -> LDAPHelper.createLDAPHelper( nodeService, pwmDomain ) );
    }

    @Override
    public Map<String, StoredNodeData> readStoredData( ) throws PwmUnrecoverableException
    {
        final Map<String, StoredNodeData> returnData = new LinkedHashMap<>(  );

        final LDAPHelper ldapHelper = ldapHelperSupplier.call();
        try
        {
            final Set<String> values = ldapHelper.getChaiUser().readMultiStringAttribute( ldapHelper.getAttr() );
            for ( final String value : values )
            {
                if ( value.startsWith( VALUE_PREFIX ) )
                {
                    final String rawValue = value.substring( VALUE_PREFIX.length() );
                    final StoredNodeData storedNodeData = JsonFactory.get().deserialize( rawValue, StoredNodeData.class );
                    returnData.put( storedNodeData.getInstanceID(),  storedNodeData );
                }
            }
        }
        catch ( final ChaiException e )
        {
            throw new PwmUnrecoverableException( PwmError.ERROR_LDAP_DATA_ERROR, "error reading node service data "
                    + ldapHelper.debugInfo() + ", error: " + e.getMessage() );
        }

        return returnData;
    }

    @Override
    public void writeNodeStatus( final StoredNodeData storedNodeData ) throws PwmUnrecoverableException
    {
        final Map<String, StoredNodeData> currentServerData = readStoredData();
        final StoredNodeData removeNode = currentServerData.get( storedNodeData.getInstanceID() );

        final LDAPHelper ldapHelper = ldapHelperSupplier.call();

        final String newRawValue = VALUE_PREFIX + JsonFactory.get().serialize( storedNodeData );

        try
        {
            if ( removeNode != null )
            {
                final String oldRawValue = VALUE_PREFIX + JsonFactory.get().serialize( removeNode );
                ldapHelper.getChaiUser().replaceAttribute( ldapHelper.getAttr(), oldRawValue, newRawValue );
            }
            else
            {
                ldapHelper.getChaiUser().addAttribute( ldapHelper.getAttr(), newRawValue );
            }
        }
        catch ( final ChaiException e )
        {
            throw new PwmUnrecoverableException( PwmError.ERROR_LDAP_DATA_ERROR, "error writing node service data "
                    + ldapHelper.debugInfo() + ", error: " + e.getMessage() );
        }

    }

    @Override
    public int purgeOutdatedNodes( final TimeDuration maxNodeAge ) throws PwmUnrecoverableException
    {
        final LDAPHelper ldapHelper = ldapHelperSupplier.call();

        int nodesPurged = 0;

        final Map<String, StoredNodeData> nodeDatas = readStoredData();

        for ( final StoredNodeData storedNodeData : nodeDatas.values() )
        {
            final TimeDuration recordAge = TimeDuration.fromCurrent( storedNodeData.getTimestamp() );
            final String instanceID = storedNodeData.getInstanceID();

            if ( recordAge.isLongerThan( maxNodeAge ) )
            {
                // purge outdated records
                LOGGER.debug( () -> "purging outdated node reference to instanceID '" + instanceID + "'" );

                try
                {
                    final String oldRawValue = VALUE_PREFIX + JsonFactory.get().serialize( storedNodeData );
                    ldapHelper.getChaiUser().deleteAttribute( ldapHelper.getAttr(), oldRawValue );
                    nodesPurged++;
                }
                catch ( final ChaiException e )
                {
                    throw new PwmUnrecoverableException( PwmError.ERROR_LDAP_DATA_ERROR, "error purging node service data "
                            + ldapHelper.debugInfo() + ", error: " + e.getMessage() );
                }
            }
        }

        return nodesPurged;

    }

    @Value
    private static class LDAPHelper
    {
        private final NodeService nodeService;
        private final PwmDomain pwmDomain;
        private final UserIdentity userIdentity;
        private final ChaiUser chaiUser;
        private final String attr;

        private LDAPHelper( final NodeService nodeService, final PwmDomain pwmDomain )
                throws PwmUnrecoverableException
        {
            this.nodeService = nodeService;
            this.pwmDomain = pwmDomain;

            final ProfileID ldapProfileID = pwmDomain.getConfig().getDefaultLdapProfile().getId();

            userIdentity = pwmDomain.getConfig().getDefaultLdapProfile().getTestUser( nodeService.getSessionLabel(), pwmDomain )
                    .orElseThrow( () ->
                    {
                        final String errorMsg = "a test user is not configured for ldap profile '" + ldapProfileID + "'";
                        final ErrorInformation errorInformation = new ErrorInformation( PwmError.CONFIG_FORMAT_ERROR, errorMsg );
                        return new PwmUnrecoverableException( errorInformation );
                    } );

            chaiUser = pwmDomain.getProxiedChaiUser( nodeService.getSessionLabel(), userIdentity );

            attr = userIdentity.getLdapProfile( pwmDomain.getPwmApplication().getConfig() ).readSettingAsString( PwmSetting.LDAP_ATTRIBUTE_PWNOTIFY );
        }

        static LDAPHelper createLDAPHelper( final NodeService nodeService, final PwmDomain pwmDomain ) throws PwmUnrecoverableException
        {
            return new LDAPHelper( nodeService, pwmDomain );
        }

        String debugInfo()
        {
            return "user '" + this.userIdentity.toDisplayString() + "' attribute '" + attr  + "'";
        }
    }
}
