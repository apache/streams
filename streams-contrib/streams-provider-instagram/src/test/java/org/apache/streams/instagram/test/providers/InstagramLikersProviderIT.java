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

package org.apache.streams.instagram.test.providers;

import org.apache.streams.instagram.provider.InstagramLikersProvider;

import org.hamcrest.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration Test for InstagramLikersProvider.
 */
public class InstagramLikersProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramLikersProviderIT.class);

  @Test(groups = "InstagramLikersProviderIT")
  public void testInstagramLikersProvider() throws Exception {

    String configfile = "./target/test-classes/InstagramLikersProviderIT.conf";
    String outfile = "./target/test-classes/InstagramLikersProviderIT.stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    Thread testThread = new Thread(() -> {
      try {
        InstagramLikersProvider.main(args);
      } catch ( Exception ex ) {
        LOGGER.error("Test Exception!", ex);
      }
    });
    testThread.start();
    testThread.join(60000);

    File out = new File(outfile);
    Assert.assertTrue(out.exists());
    Assert.assertTrue(out.canRead());
    Assert.assertTrue(out.isFile());

    FileReader outReader = new FileReader(out);
    LineNumberReader outCounter = new LineNumberReader(outReader);

    while (outCounter.readLine() != null) {}

    assertThat(outCounter.getLineNumber(), Matchers.greaterThanOrEqualTo(1));

  }
}
