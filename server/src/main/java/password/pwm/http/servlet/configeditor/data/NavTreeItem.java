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

package password.pwm.http.servlet.configeditor.data;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
@Builder( toBuilder = true )
public class NavTreeItem implements Serializable
{
    private final String id;
    private final String name;
    private final String parent;
    private final String category;
    private final String profile;
    private final NavItemType type;
    private final String profileSetting;
    private final String menuLocation;
    private final List<String> keys;

    public enum NavItemType
    {
        category,
        navigation,
        displayText,
        profile,
        profileDefinition,
    }
}
