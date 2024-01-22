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

import lombok.Value;

import java.util.List;

@Value
public class DisplayElement
{
    private final String key;
    private final Type type;
    private final String label;
    private final String value;
    private final List<String> values;

    public enum Type
    {
        string,
        timestamp,
        number,
        multiString,
    }

    public DisplayElement( final String key, final Type type, final String label, final String value )
    {
        this.key = key;
        this.type = type;
        this.label = label;
        this.value = value;
        this.values = null;
    }

    public DisplayElement( final String key, final Type type, final String label, final List<String> values )
    {
        this.key = key;
        this.type = type;
        this.label = label;
        this.value = null;
        this.values = values;
    }
}
