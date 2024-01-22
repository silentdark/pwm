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

package password.pwm.util.debug;

import password.pwm.PwmConstants;
import password.pwm.PwmDomain;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.svc.PwmService;
import password.pwm.svc.cache.CacheService;
import password.pwm.util.json.JsonFactory;
import password.pwm.util.json.JsonProvider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

final class CacheServiceDebugItemGenerator implements DomainItemGenerator
{
    @Override
    public String getFilename()
    {
        return "cache-service-info.json";
    }

    @Override
    public void outputItem( final DomainDebugItemRequest debugItemInput, final OutputStream outputStream )
            throws IOException, PwmUnrecoverableException
    {
        final PwmDomain pwmApplication = debugItemInput.pwmDomain();
        final CacheService cacheService = pwmApplication.getCacheService();

        final Map<String, Object> debugOutput = new LinkedHashMap<>();

        if ( cacheService != null && cacheService.status() == PwmService.STATUS.OPEN )
        {
            debugOutput.putAll( cacheService.debugInfo() );
        }
        else
        {
            debugOutput.put( "status", "closed" );
        }

        outputStream.write( JsonFactory.get().serializeMap( debugOutput, JsonProvider.Flag.PrettyPrint ).getBytes( PwmConstants.DEFAULT_CHARSET ) );
    }
}
