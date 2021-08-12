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

package password.pwm.svc.wordlist;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WordlistUtilTest
{
    @Test
    public void testChunkWordSized()
    {
        final String input = "zoogam";
        final Set<String> expectedOutput = new HashSet<>( Arrays.asList( "zoo", "oog", "gam", "oga", "gam" ) );
        final Set<String> output = WordlistUtil.chunkWord( input, 3 );
        Assert.assertEquals( expectedOutput, output );
    }

    @Test
    public void testChunkWordNoSize()
    {
        final String input = "zoogam";
        final Set<String> expectedOutput = new HashSet<>( Collections.singletonList( "zoogam" ) );
        final Set<String> output = WordlistUtil.chunkWord( input, 0 );
        Assert.assertEquals( expectedOutput, output );
    }
}
