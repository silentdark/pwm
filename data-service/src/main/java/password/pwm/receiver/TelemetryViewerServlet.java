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

package password.pwm.receiver;

import password.pwm.util.java.StringUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@WebServlet(
        name = "TelemetryViewer",
        urlPatterns = {
                "/viewer",
        }
)
public class TelemetryViewerServlet extends HttpServlet
{
    private static final String PARAM_DAYS = "days";

    public static final String SUMMARY_ATTR = "SummaryBean";

    @Override
    protected void doGet( final HttpServletRequest req, final HttpServletResponse resp )
            throws ServletException, IOException
    {
        final String daysString = req.getParameter( PARAM_DAYS );
        final int days = StringUtil.isEmpty( daysString ) ? 30 : Integer.parseInt( daysString );
        final ContextManager contextManager = ContextManager.getContextManager( req.getServletContext() );

        final PwmReceiverApp app = contextManager.getApp();
        app.getStatisticCounterBundle().increment( PwmReceiverApp.CounterStatsKey.TelemetryViewRequests );
        app.getStatisticEpsBundle().markEvent( PwmReceiverApp.EpsStatKey.TelemetryViewRequests );

        {
            final String errorState = app.getStatus().getErrorState();
            if ( StringUtil.notEmpty( errorState ) )
            {
                resp.sendError( 500, errorState );
                return;
            }
        }

        final Storage storage = app.getStorage();
        final SummaryBean summaryBean = SummaryBean.fromStorage( storage, Duration.of( days, ChronoUnit.DAYS ) );
        req.setAttribute( SUMMARY_ATTR, summaryBean );
        req.getServletContext().getRequestDispatcher( "/WEB-INF/jsp/telemetry-viewer.jsp" ).forward( req, resp );
    }
}
