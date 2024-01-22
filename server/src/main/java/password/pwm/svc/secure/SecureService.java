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

package password.pwm.svc.secure;

import password.pwm.error.PwmUnrecoverableException;
import password.pwm.util.secure.PwmHashAlgorithm;
import password.pwm.util.secure.PwmSecurityKey;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface SecureService
{
    <T> T decryptObject( String value, PwmSecurityKey securityKey, Class<T> returnClass ) throws PwmUnrecoverableException;

    String hash(
            PwmHashAlgorithm pwmHashAlgorithm,
            String input
    )
            throws PwmUnrecoverableException;

    String hash(
            byte[] input
    )
            throws PwmUnrecoverableException;

    String hash(
            InputStream input
    )
            throws PwmUnrecoverableException;

    String hash(
            Path file
    )
            throws IOException, PwmUnrecoverableException;

    String encryptObjectToString(
            Object object
    )
            throws PwmUnrecoverableException;

    String encryptObjectToString( Object object, PwmSecurityKey securityKey )
            throws PwmUnrecoverableException;

    <T> T decryptObject(
            String value,
            Class<T> returnClass
    )
            throws PwmUnrecoverableException;

}
