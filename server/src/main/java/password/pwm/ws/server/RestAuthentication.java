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

package password.pwm.ws.server;

import com.novell.ldapchai.provider.ChaiProvider;
import lombok.Value;
import password.pwm.bean.UserIdentity;
import password.pwm.config.option.WebServiceUsage;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

@Value
public class RestAuthentication implements Serializable
{
    private static final long serialVersionUID = 1L;

    private RestAuthenticationType type;
    private String namedSecretName;
    private UserIdentity ldapIdentity;
    private Set<WebServiceUsage> usages;
    private boolean thirdPartyEnabled;
    private transient ChaiProvider chaiProvider;

    public final Object readObject( ) throws IOException, ClassNotFoundException
    {
        throw new IOException( "class can not be de-serialized" );
    }
}
