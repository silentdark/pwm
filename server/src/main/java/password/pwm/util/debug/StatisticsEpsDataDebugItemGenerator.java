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

import org.apache.commons.csv.CSVPrinter;
import password.pwm.PwmApplication;
import password.pwm.svc.stats.EpsStatistic;
import password.pwm.svc.stats.Statistic;
import password.pwm.svc.stats.StatisticsService;
import password.pwm.util.java.PwmUtil;
import password.pwm.util.java.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

final class StatisticsEpsDataDebugItemGenerator implements AppItemGenerator
{
    @Override
    public String getFilename()
    {
        return "statistics-eps.csv";
    }

    @Override
    public void outputItem( final AppDebugItemRequest debugItemInput, final OutputStream outputStream )
            throws IOException

    {
        final PwmApplication pwmDomain = debugItemInput.pwmApplication();
        final StatisticsService statsManager = pwmDomain.getStatisticsService();
        final CSVPrinter csvPrinter = PwmUtil.makeCsvPrinter( outputStream );
        {
            final List<String> headerRow = new ArrayList<>();
            headerRow.add( "Counter" );
            headerRow.add( "Duration" );
            headerRow.add( "Events/Second" );
            csvPrinter.printComment( StringUtil.join( headerRow, "," ) );
        }
        for ( final EpsStatistic epsStatistic : EpsStatistic.values() )
        {
            for ( final Statistic.EpsDuration epsDuration : Statistic.EpsDuration.values() )
            {
                try
                {
                    final List<String> dataRow = new ArrayList<>();
                    final BigDecimal value = statsManager.readEps( epsStatistic, epsDuration );
                    final String sValue = value.toPlainString();
                    dataRow.add( epsStatistic.getLabel( debugItemInput.locale() ) );
                    dataRow.add( epsDuration.getTimeDuration().asCompactString() );
                    dataRow.add( sValue );
                    csvPrinter.printRecord( dataRow );
                }
                catch ( final Exception e )
                {
                    debugItemInput.logger().error( this, () -> "error generating csv-stats summary info: " + e.getMessage() );
                }
            }
        }
        csvPrinter.flush();
    }
}
