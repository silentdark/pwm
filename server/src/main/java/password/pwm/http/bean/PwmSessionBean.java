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

package password.pwm.http.bean;

import lombok.Getter;
import lombok.Setter;
import password.pwm.config.option.SessionBeanMode;
import password.pwm.error.ErrorInformation;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public abstract class PwmSessionBean implements Serializable
{
    public enum BeanType
    {
        PUBLIC,
        AUTHENTICATED,
    }

    private static List<Class<? extends PwmSessionBean>> publicBeans = List.of(
            ActivateUserBean.class,
            ForgottenPasswordBean.class,
            NewUserBean.class );


    private String guid;
    private Instant timestamp;
    private ErrorInformation lastError;

    public abstract BeanType getBeanType( );

    public abstract Set<SessionBeanMode> supportedModes( );

    public static List<Class<? extends PwmSessionBean>> getPublicBeans()
    {
        return publicBeans;
    }
}
