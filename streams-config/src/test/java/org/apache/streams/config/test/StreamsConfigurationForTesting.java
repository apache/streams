package org.apache.streams.config.test;

import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.StreamsConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.juneau.annotation.BeanProperty;

public class StreamsConfigurationForTesting extends StreamsConfiguration {

  @JsonProperty("componentOne")
  @BeanProperty("componentOne")
  private org.apache.streams.config.ComponentConfiguration componentOne;

  @JsonProperty("componentTwo")
  @BeanProperty("componentTwo")
  private org.apache.streams.config.ComponentConfiguration componentTwo;

  public StreamsConfigurationForTesting() {
  }

  public StreamsConfigurationForTesting(ComponentConfiguration componentOne, ComponentConfiguration componentTwo) {
    this.componentOne = componentOne;
    this.componentTwo = componentTwo;
  }

  @JsonProperty("componentOne")
  @BeanProperty("componentOne")
  public ComponentConfiguration getComponentOne() {
    return componentOne;
  }

  @JsonProperty("componentOne")
  @BeanProperty("componentOne")
  public void setComponentOne(ComponentConfiguration componentOne) {
    this.componentOne = componentOne;
  }

  @JsonProperty("componentTwo")
  @BeanProperty("componentTwo")
  public ComponentConfiguration getComponentTwo() {
    return componentTwo;
  }

  @JsonProperty("componentTwo")
  @BeanProperty("componentTwo")
  public void setComponentTwo(ComponentConfiguration componentTwo) {
    this.componentTwo = componentTwo;
  }
}
