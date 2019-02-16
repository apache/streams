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

import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.twitter.provider.SevenDaySearchProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

/*
 This IT is disabled by default because the number of results it gets to too dependant on circumstances.
 */
@Test(dependsOnGroups = {"Account"}, enabled = false, groups = {"Providers"})
public class TwitterSevenDaySearchProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSevenDaySearchProviderIT.class);

  @Test(groups = "TwitterSevenDaySearchProviderIT")
  public void testTwitterSevenDaySearchProvider() throws Exception {

    String configfile = "./target/test-classes/TwitterSevenDaySearchProviderIT.conf";
    String outfile = "./target/test-classes/TwitterSevenDaySearchProviderIT.stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    Thread testThread = new Thread(() -> {
      try {
        SevenDaySearchProvider.main(args);
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

    Integer max_items = StreamsConfigurator.getConfig().getInt("org.apache.streams.twitter.config.SevenDaySearchProviderConfiguration.max_items");

    Assert.assertEquals (outCounter.getLineNumber(), max_items.intValue());

  }
}
