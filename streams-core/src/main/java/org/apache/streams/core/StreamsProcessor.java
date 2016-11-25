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

package org.apache.streams.core;

import java.util.List;

public interface StreamsProcessor extends StreamsOperation {

  /**
   * Process/Analyze the {@link org.apache.streams.core.StreamsDatum} and return the the StreamsDatums that will
   * passed to every down stream operation that reads from this processor.
   * @param entry StreamsDatum to be processed
   * @return resulting StreamDatums from processing. Should never be null or contain null object.  Empty list OK.
   */
  List<StreamsDatum> process( StreamsDatum entry );

}
