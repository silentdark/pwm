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

package password.pwm.svc.db;

import password.pwm.AppProperty;
import password.pwm.config.AppConfig;
import password.pwm.config.PwmSetting;
import password.pwm.config.value.FileValue;
import password.pwm.data.ImmutableByteArray;
import password.pwm.util.PasswordData;

import java.util.Map;

public record DBConfiguration(
        String driverClassname,
        String connectionString,
        String username,
        PasswordData password,
        String columnTypeKey,
        String columnTypeValue,
        ImmutableByteArray jdbcDriver,
        int maxConnections,
        int connectionTimeout,
        int keyColumnLength,
        boolean failOnIndexCreation,
        boolean traceLogging
)
{
    public ImmutableByteArray getJdbcDriver()
    {
        return jdbcDriver;
    }

    static DBConfiguration fromConfiguration( final AppConfig config )
    {
        final Map<FileValue.FileInformation, FileValue.FileContent> fileValue = config.readSettingAsFile(
                PwmSetting.DATABASE_JDBC_DRIVER );
        final ImmutableByteArray jdbcDriverBytes;
        if ( fileValue != null && !fileValue.isEmpty() )
        {
            final FileValue.FileContent fileContent = fileValue.values().iterator().next();
            jdbcDriverBytes = fileContent.getContents();
        }
        else
        {
            jdbcDriverBytes = null;
        }

        final int maxConnections = Integer.parseInt( config.readAppProperty( AppProperty.DB_CONNECTIONS_MAX ) );
        final int connectionTimeout = Integer.parseInt( config.readAppProperty( AppProperty.DB_CONNECTIONS_TIMEOUT_MS ) );

        final int keyColumnLength = Integer.parseInt( config.readAppProperty( AppProperty.DB_SCHEMA_KEY_LENGTH ) );

        final boolean haltOnIndexCreateError = Boolean.parseBoolean( config.readAppProperty( AppProperty.DB_INIT_HALT_ON_INDEX_CREATE_ERROR ) );

        return new DBConfiguration(
                config.readSettingAsString( PwmSetting.DATABASE_CLASS ),
                config.readSettingAsString( PwmSetting.DATABASE_URL ),
                config.readSettingAsString( PwmSetting.DATABASE_USERNAME ),
                config.readSettingAsPassword( PwmSetting.DATABASE_PASSWORD ),
                config.readSettingAsString( PwmSetting.DATABASE_COLUMN_TYPE_KEY ),
                config.readSettingAsString( PwmSetting.DATABASE_COLUMN_TYPE_VALUE ),
                jdbcDriverBytes,
                maxConnections,
                connectionTimeout,
                keyColumnLength,
                haltOnIndexCreateError,
                config.readSettingAsBoolean( PwmSetting.DATABASE_DEBUG_TRACE )
        );
    }
}
