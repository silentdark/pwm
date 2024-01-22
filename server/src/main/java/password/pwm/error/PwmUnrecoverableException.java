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

package password.pwm.error;


import com.novell.ldapchai.exception.ChaiException;
import com.novell.ldapchai.exception.ChaiUnavailableException;

import java.io.IOException;

/**
 * A general exception thrown by PWM.
 */
public class PwmUnrecoverableException extends PwmException
{

    public PwmUnrecoverableException( final ErrorInformation error )
    {
        super( error );
    }

    public PwmUnrecoverableException( final ErrorInformation error, final Throwable initialCause )
    {
        super( error, initialCause );
    }

    public PwmUnrecoverableException( final PwmError error )
    {
        super( error );
    }

    public PwmUnrecoverableException( final PwmError error, final String detailedErrorMsg )
    {
        super( error, detailedErrorMsg );
    }

    public static PwmUnrecoverableException fromChaiException( final ChaiException e )
    {
        final ErrorInformation errorInformation;
        if ( e instanceof ChaiUnavailableException )
        {
            errorInformation = new ErrorInformation( PwmError.ERROR_DIRECTORY_UNAVAILABLE, e.getMessage() );
        }
        else
        {
            errorInformation = new ErrorInformation( PwmError.forChaiError( e.getErrorCode() )
                    .orElse( PwmError.ERROR_INTERNAL ), e.getMessage() );
        }
        return new PwmUnrecoverableException( errorInformation );
    }

    public static PwmUnrecoverableException newException( final PwmError error, final String message )
    {
        return new PwmUnrecoverableException( new ErrorInformation( error, message ) );
    }

    public static PwmUnrecoverableException newException( final PwmError error )
    {
        return new PwmUnrecoverableException( new ErrorInformation( error ) );
    }

    public static PwmUnrecoverableException convert( final IOException e )
    {
        final String msg = "unexpected io error: " + e.getMessage();
        return PwmUnrecoverableException.newException( PwmError.ERROR_INTERNAL, msg );
    }
}

