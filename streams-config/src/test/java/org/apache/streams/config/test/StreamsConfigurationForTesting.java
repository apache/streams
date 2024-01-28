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

package org.apache.streams.config.test;

import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.StreamsConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.juneau.annotation.Beanp;

public class StreamsConfigurationForTesting extends StreamsConfiguration {

  @JsonProperty("componentOne")
  @Beanp("componentOne")
  private org.apache.streams.config.ComponentConfiguration componentOne;

  @JsonProperty("componentTwo")
  @Beanp("componentTwo")
  private org.apache.streams.config.ComponentConfiguration componentTwo;

  public StreamsConfigurationForTesting() {
  }

  public StreamsConfigurationForTesting(ComponentConfiguration componentOne, ComponentConfiguration componentTwo) {
    this.componentOne = componentOne;
    this.componentTwo = componentTwo;
  }

  @JsonProperty("componentOne")
  @Beanp("componentOne")
  public ComponentConfiguration getComponentOne() {
    return componentOne;
  }

  @JsonProperty("componentOne")
  @Beanp("componentOne")
  public void setComponentOne(ComponentConfiguration componentOne) {
    this.componentOne = componentOne;
  }

  @JsonProperty("componentTwo")
  @Beanp("componentTwo")
  public ComponentConfiguration getComponentTwo() {
    return componentTwo;
  }

  @JsonProperty("componentTwo")
  @Beanp("componentTwo")
  public void setComponentTwo(ComponentConfiguration componentTwo) {
    this.componentTwo = componentTwo;
  }
}
