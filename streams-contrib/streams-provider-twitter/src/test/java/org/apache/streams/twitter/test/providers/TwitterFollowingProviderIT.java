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
import org.apache.streams.twitter.provider.TwitterFollowingProvider;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

@Test(dependsOnGroups = {"Account"}, groups = {"Providers"})
public class TwitterFollowingProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProviderIT.class);

  /**
   * Data Provider for TwitterFollowingProviderIT
   *   [][0] = endpoint
   *   [][1] = ids_only
   *   [][2] = max_items
   * @return
   */
  @DataProvider(name = "TwitterFollowingProviderIT")
  public static Object[][] credentials() {
    return new Object[][] {
        {"followers", Boolean.FALSE, 40},
        {"followers", Boolean.TRUE, 10000},
        {"friends", Boolean.FALSE, 40 },
        {"friends", Boolean.TRUE, 100}
    };
  }

  @Test(dataProvider = "TwitterFollowingProviderIT")
  public void testTwitterFollowingProvider(String endpoint, Boolean ids_only, Integer max_items) throws Exception {

    String configfile = "./target/test-classes/TwitterFollowingProviderIT.conf";
    String outfile = "./target/test-classes/TwitterFollowingProviderIT-"+endpoint+"-"+ids_only+".stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    File conf = new File(configfile);
    Assert.assertTrue (conf.exists());
    Assert.assertTrue (conf.canRead());
    Assert.assertTrue (conf.isFile());

    System.setProperty("ENDPOINT", endpoint);
    System.setProperty("IDS_ONLY", Boolean.toString(ids_only));
    System.setProperty("MAX_ITEMS", Integer.toString(max_items));
    ConfigFactory.invalidateCaches();
    StreamsConfigurator.setConfig(ConfigFactory.load());

    Thread testThread = new Thread(() -> {
      try {
        TwitterFollowingProvider.main(args);
      } catch ( Exception ex ) {
        LOGGER.error("Test Exception!", ex);
      }
    });
    testThread.start();
    testThread.join(60000);

    File out = new File(outfile);
    Assert.assertTrue (out.exists());
    Assert.assertTrue (out.canRead());
    Assert.assertTrue (out.isFile());

    FileReader outReader = new FileReader(out);
    LineNumberReader outCounter = new LineNumberReader(outReader);

    while (outCounter.readLine() != null) {}

    Assert.assertEquals (outCounter.getLineNumber(), max_items.intValue());

  }
}
