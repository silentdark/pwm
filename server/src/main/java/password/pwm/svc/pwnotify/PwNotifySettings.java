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

package password.pwm.svc.pwnotify;

import lombok.Builder;
import lombok.Value;
import password.pwm.AppProperty;
import password.pwm.config.DomainConfig;
import password.pwm.config.PwmSetting;
import password.pwm.util.java.TimeDuration;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
class PwNotifySettings implements Serializable
{
    private final List<Integer> notificationIntervals;
    private final TimeDuration maximumSkipWindow;
    private final TimeDuration zuluOffset;
    private final int maxLdapSearchSize;
    private final TimeDuration searchTimeout;
    private final int batchCount;
    private final BigDecimal batchTimeMultiplier;

    static PwNotifySettings fromConfiguration( final DomainConfig domainConfig )
    {
        final PwNotifySettingsBuilder builder = PwNotifySettings.builder();
        {
            final List<Integer> timeDurations = domainConfig.readSettingAsStringArray( PwmSetting.PW_EXPY_NOTIFY_INTERVAL ).stream()
                    .map( Integer::parseInt )
                    .sorted()
                    .collect( Collectors.toUnmodifiableList() );

            builder.notificationIntervals( timeDurations );
        }

        builder.searchTimeout( TimeDuration.of( Long.parseLong( domainConfig.readAppProperty( AppProperty.REPORTING_LDAP_SEARCH_TIMEOUT_MS ) ), TimeDuration.Unit.MILLISECONDS ) );
        builder.zuluOffset( TimeDuration.of( domainConfig.readSettingAsLong( PwmSetting.PW_EXPY_NOTIFY_JOB_OFFSET ), TimeDuration.Unit.SECONDS ) );
        builder.batchCount( Integer.parseInt( domainConfig.readAppProperty( AppProperty.PWNOTIFY_BATCH_COUNT ) ) );
        builder.maxLdapSearchSize( Integer.parseInt( domainConfig.readAppProperty( AppProperty.PWNOTIFY_MAX_LDAP_SEARCH_SIZE ) ) );
        builder.batchTimeMultiplier( new BigDecimal( domainConfig.readAppProperty( AppProperty.PWNOTIFY_BATCH_DELAY_TIME_MULTIPLIER ) ) );
        builder.maximumSkipWindow( TimeDuration.of(
                Long.parseLong( domainConfig.readAppProperty( AppProperty.PWNOTIFY_MAX_SKIP_RERUN_WINDOW_SECONDS ) ), TimeDuration.Unit.SECONDS ) );

        return builder.build();
    }
}
