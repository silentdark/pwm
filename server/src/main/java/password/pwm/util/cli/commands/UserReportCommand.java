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

package password.pwm.util.cli.commands;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import password.pwm.PwmApplication;
import password.pwm.PwmConstants;
import password.pwm.bean.DomainID;
import password.pwm.bean.SessionLabel;
import password.pwm.error.PwmUnrecoverableException;
import password.pwm.health.HealthRecord;
import password.pwm.svc.PwmService;
import password.pwm.svc.report.ReportProcess;
import password.pwm.svc.report.ReportProcessRequest;
import password.pwm.svc.report.ReportService;
import password.pwm.util.cli.CliParameters;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class UserReportCommand extends AbstractCliCommand
{
    private static final String OUTPUT_FILE_OPTIONNAME = "outputFile";

    @Override
    @SuppressFBWarnings( "DM_EXIT" )
    void doCommand( )
            throws IOException
    {
        final Path outputFile = ( Path ) cliEnvironment.getOptions().get( OUTPUT_FILE_OPTIONNAME );

        try ( OutputStream outputFileStream = Files.newOutputStream( outputFile ) )
        {

            final PwmApplication pwmApplication = cliEnvironment.getPwmApplication();

            final ReportService reportService = pwmApplication.domains().get( DomainID.DOMAIN_ID_DEFAULT ).getReportService();
            if ( reportService.status() != PwmService.STATUS.OPEN )
            {
                out( "report service is not open or enabled" );
                final List<HealthRecord> healthIssues = reportService.healthCheck();
                if ( healthIssues != null )
                {
                    for ( final HealthRecord record : healthIssues )
                    {
                        out( "report health status: " + record.toDebugString( Locale.getDefault(), pwmApplication.getConfig() ) );
                    }
                }
                return;
            }

            final ReportProcessRequest reportProcessRequest = ReportProcessRequest.builder()
                    .locale( PwmConstants.DEFAULT_LOCALE )
                    .sessionLabel( SessionLabel.CLI_SESSION_LABEL )
                    .build();

            try ( ReportProcess reportProcess = reportService.createReportProcess( reportProcessRequest ) )
            {
                reportProcess.startReport( outputFileStream );
            }
        }
        catch ( final IOException | PwmUnrecoverableException e )
        {
            out( "unable to open file '" + outputFile + "' for writing" );
            System.exit( -1 );
        }

        out( "report output complete." );
    }

    @Override
    public CliParameters getCliParameters( )
    {
        final CliParameters.Option outputFileOption = new CliParameters.Option()
        {
            @Override
            public boolean isOptional( )
            {
                return false;
            }

            @Override
            public Type getType( )
            {
                return Type.NEW_FILE;
            }

            @Override
            public String getName( )
            {
                return OUTPUT_FILE_OPTIONNAME;
            }
        };


        final CliParameters cliParameters = new CliParameters();
        cliParameters.commandName = "ExportUserReportDetail";
        cliParameters.description = "Output user report details to the output file (csv format)";
        cliParameters.options = Collections.singletonList( outputFileOption );

        cliParameters.needsPwmApplication = true;
        cliParameters.readOnly = false;

        return cliParameters;
    }

}
