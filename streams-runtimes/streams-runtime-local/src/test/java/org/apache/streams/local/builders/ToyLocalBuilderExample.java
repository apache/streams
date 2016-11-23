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

package org.apache.streams.local.builders;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.test.processors.DoNothingProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DoNothingWriter;



/**
 * Created by rebanks on 2/20/14.
 */
public class ToyLocalBuilderExample {

  /**
   * A simple example of how to run a stream in local mode.
   * @param args
   */
  public static void main(String[] args) {
    StreamBuilder builder = new LocalStreamBuilder();
    builder.newReadCurrentStream("prov", new NumericMessageProvider(1000000))
        .addStreamsProcessor("proc", new DoNothingProcessor(), 100, "prov")
        .addStreamsPersistWriter("writer", new DoNothingWriter(), 3, "proc");
    builder.start();
  }

}
