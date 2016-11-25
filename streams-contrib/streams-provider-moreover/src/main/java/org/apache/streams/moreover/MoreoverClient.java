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

package org.apache.streams.moreover;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * MoreoverClient is a Client for Moreover.
 */
public class MoreoverClient {
  private static final Logger logger = LoggerFactory.getLogger(MoreoverClient.class);

  private static final String BASE_URL = "http://metabase.moreover.com/api/v10/articles?key=%s&limit=%s&sequence_id=%s";
  private final String id;
  private String apiKey;
  private BigInteger lastSequenceId = BigInteger.ZERO;
  //testing purpose only
  public long pullTime;
  private boolean debug;

  /**
   * MoreoverClient constructor.
   * @param id id
   * @param apiKey apiKey
   * @param sequence sequence
   */
  public MoreoverClient(String id, String apiKey, String sequence) {
    logger.info("Constructed new client for id:{} key:{} sequence:{}", id, apiKey, sequence);
    this.id = id;
    this.apiKey = apiKey;
    this.lastSequenceId = new BigInteger(sequence);
  }

  /**
   * get limit ArticlesAfter sequenceId.
   * @param sequenceId sequenceId
   * @param limit limit
   * @return MoreoverResult
   * @throws IOException IOException
   */
  public MoreoverResult getArticlesAfter(String sequenceId, int limit) throws IOException {
    String urlString = String.format(BASE_URL, this.apiKey, limit, sequenceId);
    logger.debug("Making call to {}", urlString);
    long start = System.nanoTime();
    MoreoverResult result = new MoreoverResult(id, getArticles(new URL(urlString)), start, System.nanoTime());
    if (!result.getMaxSequencedId().equals(BigInteger.ZERO)) {
      this.lastSequenceId = result.getMaxSequencedId();
      logger.debug("Maximum sequence from last call {}", this.lastSequenceId);
    } else {
      logger.debug("No maximum sequence returned in last call {}", this.lastSequenceId);
    }
    return result;
  }

  public MoreoverResult getNextBatch() throws IOException {
    logger.debug("Getting next results for {} {} {}", this.id, this.apiKey, this.lastSequenceId);
    return getArticlesAfter(this.lastSequenceId.toString(), 500);
  }

  private String getArticles2(URL url) throws IOException {
    HttpURLConnection cn = (HttpURLConnection) url.openConnection();
    cn.setRequestMethod("GET");
    cn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
    cn.setDoInput(true);
    cn.setDoOutput(false);
    BufferedReader reader = new BufferedReader(new InputStreamReader(cn.getInputStream(), Charset.forName("UTF-8")));
    String line = null;
    StringBuilder builder = new StringBuilder();
    String result = new String("".getBytes(Charset.forName("UTF-8")), Charset.forName("UTF-8"));
    while ((line = reader.readLine()) != null) {
      result += line;
    }
    pullTime = new Date().getTime();
    return result;
  }

  private String getArticles(URL url) throws IOException {
    HttpURLConnection cn = (HttpURLConnection) url.openConnection();
    cn.setRequestMethod("GET");
    cn.addRequestProperty("Content-Type", "text/xml;charset=UTF-8");
    cn.setDoInput(true);
    cn.setDoOutput(false);
    StringWriter writer = new StringWriter();
    IOUtils.copy(new InputStreamReader(cn.getInputStream(), Charset.forName("UTF-8")), writer);
    writer.flush();
    pullTime = new Date().getTime();

    // added after seeing java.net.SocketException: Too many open files
    cn.disconnect();

    return writer.toString();
  }
}
