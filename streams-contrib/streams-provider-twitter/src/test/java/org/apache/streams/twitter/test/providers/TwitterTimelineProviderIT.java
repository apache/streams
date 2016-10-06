/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.twitter.test.providers;

import com.google.common.collect.Lists;
import org.apache.streams.twitter.provider.TwitterTimelineProvider;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

public class TwitterTimelineProviderIT {

    @Test
    public void testTwitterTimelineProvider() throws Exception {

        String configfile = "./target/test-classes/TwitterTimelineProviderIT.conf";
        String outfile = "./target/test-classes/TwitterTimelineProviderIT.stdout.txt";

        TwitterTimelineProvider.main(Lists.newArrayList(configfile, outfile).toArray(new String[2]));

        File out = new File(outfile);
        assert (out.exists());
        assert (out.canRead());
        assert (out.isFile());

        FileReader outReader = new FileReader(out);
        LineNumberReader outCounter = new LineNumberReader(outReader);

        while(outCounter.readLine() != null) {}

        assert (outCounter.getLineNumber() == 1000);

    }
}
