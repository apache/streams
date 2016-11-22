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

package org.apache.streams.youtube.test.providers;

import com.youtube.provider.YoutubeUserActivityProvider;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;

/**
 * Integration Test for YoutubeUserActivityProvider.
 */
public class YoutubeUserActivityProviderIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeUserActivityProviderIT.class);

  @Test
  public void testYoutubeUserActivityProvider() throws Exception {

    String configfile = "./target/test-classes/YoutubeUserActivityProviderIT.conf";
    String outfile = "./target/test-classes/YoutubeUserActivityProviderIT.stdout.txt";

    String[] args = new String[2];
    args[0] = configfile;
    args[1] = outfile;

    Thread testThread = new Thread((Runnable) () -> {
      try {
        YoutubeUserActivityProvider.main(args);
      } catch ( Exception ex ) {
        LOGGER.error("Test Exception!", ex);
      }
    });
    testThread.start();
    testThread.join(60000);

    File out = new File(outfile);
    assert (out.exists());
    assert (out.canRead());
    assert (out.isFile());

    FileReader outReader = new FileReader(out);
    LineNumberReader outCounter = new LineNumberReader(outReader);

    while (outCounter.readLine() != null) {}

    assert (outCounter.getLineNumber() >= 250);

  }
}
