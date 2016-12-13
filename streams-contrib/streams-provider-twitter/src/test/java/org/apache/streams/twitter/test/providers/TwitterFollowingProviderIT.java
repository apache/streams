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

import org.apache.streams.twitter.provider.TwitterFollowingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TwitterFollowingProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProviderIT.class);

  @Test
  public void testTwitterFollowingProvider() throws Exception {

    String configfile = "./target/test-classes/TwitterFollowingProviderIT.conf";
    String outfile = "./target/test-classes/TwitterFollowingProviderIT.stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    Thread testThread = new Thread((Runnable) () -> {
      try {
        TwitterFollowingProvider.main(args);
      } catch ( Exception ex ) {
        LOGGER.error("Test Exception!", ex);
      }
    });
    testThread.start();
    testThread.join(60000);

    File out = new File(outfile);
    assertTrue (out.exists());
    assertTrue (out.canRead());
    assertTrue (out.isFile());

    FileReader outReader = new FileReader(out);
    LineNumberReader outCounter = new LineNumberReader(outReader);

    while (outCounter.readLine() != null) {}

    assertEquals (outCounter.getLineNumber(), 10000);

  }
}
