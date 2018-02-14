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

import org.apache.streams.twitter.provider.TwitterEngagersProvider;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@Test(dependsOnGroups = {"Account"}, groups = {"Providers"})
public class TwitterEngagersProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterEngagersProviderIT.class);

  @Test(groups = "TwitterEngagersProviderIT")
  public void testTwitterEngagersProvider() throws Exception {

    String configfile = "./target/test-classes/TwitterEngagersProviderIT.conf";
    String outfile = "./target/test-classes/TwitterEngagersProviderIT.stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    Thread testThread = new Thread(() -> {
      try {
        TwitterEngagersProvider.main(args);
      } catch ( Exception ex ) {
        LOGGER.error("Test Exception!", ex);
      }
    });
    testThread.start();
    testThread.join(600000);

    File out = new File(outfile);
    Assert.assertTrue (out.exists());
    Assert.assertTrue (out.canRead());
    Assert.assertTrue (out.isFile());

    FileReader outReader = new FileReader(out);
    LineNumberReader outCounter = new LineNumberReader(outReader);

    while (outCounter.readLine() != null) {}

    Assert.assertThat(outCounter.getLineNumber(), is(greaterThan(25)));

    // this should actually match max items
    // Assert.assertThat(outCounter.getLineNumber(), is(equalTo(100)));
  }
}
