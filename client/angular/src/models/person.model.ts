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


export interface IPerson {
    // Common properties
    userKey?: string;
    numDirectReports?: number;

    // Autocomplete properties (via Search)
    _displayName?: string;

    // Details properties (not available in search)
    detail?: any;
    displayNames?: string[];
    photoURL?: string;
    links?: any[];

    // Search properties (not available in details)
    givenName?: string;
    mail?: string;
    sn?: string;
    telephoneNumber?: string;
    title?: string;
}
