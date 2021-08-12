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

package password.pwm.util.localdb;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

public class LocalDBExtendedTest
{

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    private static final LocalDB.DB TEST_DB = LocalDB.DB.TEMP;
    private static LocalDB localDB;

    @BeforeClass
    public static void setUp() throws Exception
    {
        TestHelper.setupLogging();
        final File fileLocation = testFolder.newFolder( "localdb-test" );
        localDB = LocalDBFactory.getInstance( fileLocation, false, null, null );
        localDB.truncate( TEST_DB );
        Assert.assertEquals( 0, localDB.size( TEST_DB ) );
    }

    @Test
    public void testPut() throws LocalDBException
    {
        Assert.assertNull( localDB.get( TEST_DB, "testKey1" ) );
        localDB.put( TEST_DB, "testKey1", "testValue1" );
        Assert.assertEquals( localDB.get( TEST_DB, "testKey1" ), "testValue1" );
    }

    @Test
    public void testSize() throws LocalDBException
    {
        final long startTime = System.currentTimeMillis();
        for ( final LocalDB.DB loopDB : LocalDB.DB.values() )
        {
            final long size = localDB.size( loopDB );
            //System.out.println( loopDB + " size=" + size );
        }
        //System.out.println( "total duration: " + TimeDuration.fromCurrent( startTime ).asLongString() );
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        if ( localDB != null )
        {
            localDB.close();
            localDB = null;
        }
    }
}
