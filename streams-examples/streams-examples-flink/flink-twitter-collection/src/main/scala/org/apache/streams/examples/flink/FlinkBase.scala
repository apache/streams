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

package org.apache.streams.examples.flink

import java.net.MalformedURLException

import com.typesafe.config.Config
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.restartstrategy.RestartStrategies
import org.apache.flink.api.scala.ExecutionEnvironment
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.flink.{FlinkBatchConfiguration, FlinkStreamingConfiguration, StreamsFlinkConfiguration}
import org.apache.streams.hdfs.{HdfsConfiguration, HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.apache.streams.jackson.StreamsJacksonMapper
import org.slf4j.LoggerFactory

/**
  * FlinkBase is a base class with capabilities common to all of the streams flink examples.
  */
trait FlinkBase {

  private val BASELOGGER = LoggerFactory.getLogger("FlinkBase")
  private val MAPPER = StreamsJacksonMapper.getInstance()

  var configUrl : String = _
  var typesafe : Config = _
  var streamsConfig = StreamsConfigurator.detectConfiguration()
  var streamsFlinkConfiguration: StreamsFlinkConfiguration = _

  var executionEnvironment: ExecutionEnvironment = _
  var streamExecutionEnvironment: StreamExecutionEnvironment = _

  /*
   Basic stuff for every flink job
   */
  def main(args: Array[String]): Unit = {
    // if only one argument, use it as the config URL
    if( args.length > 0 ) {
      BASELOGGER.info("Args: {}", args)
      configUrl = args(0)
      setup(configUrl)
    }

  }

  def setup(configUrl : String): Boolean =  {
    BASELOGGER.info("StreamsConfigurator.config: {}", StreamsConfigurator.getConfig)
    if(StringUtils.isNotEmpty(configUrl)) {
      BASELOGGER.info("StreamsConfigurator.resolveConfig(configUrl): {}", StreamsConfigurator.resolveConfig(configUrl))
      try {
        typesafe = StreamsConfigurator.resolveConfig(configUrl).withFallback(StreamsConfigurator.getConfig).resolve()
      } catch {
        case mue: MalformedURLException => {
          BASELOGGER.error("Invalid Configuration URL: ", mue)
          return false
        }
        case e: Exception => {
          BASELOGGER.error("Invalid Configuration URL: ", e)
          return false
        }
      }
    }
    else {
      typesafe = StreamsConfigurator.getConfig
    }

    setup(typesafe)

  }

  def setup(typesafe : Config): Boolean =  {
    this.typesafe = typesafe

    BASELOGGER.info("Typesafe Config: {}", typesafe)

    if( this.typesafe.getString("mode").equals("streaming")) {
      val streamingConfiguration: FlinkStreamingConfiguration =
        new ComponentConfigurator[FlinkStreamingConfiguration](classOf[FlinkStreamingConfiguration]).detectConfiguration(typesafe)
      setupStreaming(streamingConfiguration)
    } else if( this.typesafe.getString("mode").equals("batch")) {
      val batchConfiguration: FlinkBatchConfiguration =
        new ComponentConfigurator[FlinkBatchConfiguration](classOf[FlinkBatchConfiguration]).detectConfiguration(typesafe)
      setupBatch(batchConfiguration)
    } else {
      false
    }
  }

  //  def setup(typesafe: Config): Boolean =  {
  //
  //    val streamsConfig = StreamsConfigurator.detectConfiguration(typesafe)
  //
  //    this.streamsConfig = streamsConfig
  //
  //    BASELOGGER.info("Streams Config: " + streamsConfig)
  //
  //    setup(streamsConfig)
  //  }

  def setupStreaming(streamingConfiguration: FlinkStreamingConfiguration): Boolean = {

    BASELOGGER.info("FsStreamingFlinkConfiguration: " + streamingConfiguration)

    this.streamsFlinkConfiguration = streamingConfiguration

    if( streamsFlinkConfiguration == null) return false

    if( streamExecutionEnvironment == null )
      streamExecutionEnvironment = streamEnvironment(streamingConfiguration)

    false

  }

  def setupBatch(batchConfiguration: FlinkBatchConfiguration): Boolean =  {

    BASELOGGER.info("FsBatchFlinkConfiguration: " + batchConfiguration)

    this.streamsFlinkConfiguration = batchConfiguration

    if( streamsFlinkConfiguration == null) return false

    if( executionEnvironment == null )
      executionEnvironment = batchEnvironment(batchConfiguration)

    true

  }

  def batchEnvironment(config: FlinkBatchConfiguration = new FlinkBatchConfiguration()) : ExecutionEnvironment = {
    if (config.getTest == false && config.getLocal == false) {
      val env = ExecutionEnvironment.getExecutionEnvironment
      env
    } else {
      val env = ExecutionEnvironment.createLocalEnvironment(config.getParallel.toInt)
      env
    }
  }

  def streamEnvironment(config: FlinkStreamingConfiguration = new FlinkStreamingConfiguration()) : StreamExecutionEnvironment = {
    if( config.getTest == false && config.getLocal == false) {
      val env = StreamExecutionEnvironment.getExecutionEnvironment

      env.setRestartStrategy(RestartStrategies.noRestart())

      // start a checkpoint every hour
      env.enableCheckpointing(config.getCheckpointIntervalMs)

      env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)

      // checkpoints have to complete within five minutes, or are discarded
      env.getCheckpointConfig.setCheckpointTimeout(config.getCheckpointTimeoutMs)

      // allow only one checkpoint to be in progress at the same time
      env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)

      env
    }

    else StreamExecutionEnvironment.createLocalEnvironment(config.getParallel.toInt)
  }

  def buildReaderPath(configObject: HdfsReaderConfiguration) : String = {
    var inPathBuilder : String = ""
    if (configObject.getScheme.equals(HdfsConfiguration.Scheme.FILE)) {
      inPathBuilder = configObject.getPath + "/" + configObject.getReaderPath
    }
    else if (configObject.getScheme.equals(HdfsConfiguration.Scheme.HDFS)) {
      inPathBuilder = configObject.getScheme + "://" + configObject.getHost + ":" + configObject.getPort + "/" + configObject.getPath + "/" + configObject.getReaderPath
    }
    else if (configObject.getScheme.toString.equals("s3")) {
      inPathBuilder = configObject.getScheme + "://" + configObject.getPath + "/" + configObject.getReaderPath
    } else {
      throw new Exception("scheme not recognized: " + configObject.getScheme)
    }
    inPathBuilder
  }

  def buildWriterPath(configObject: HdfsWriterConfiguration) : String = {
    var outPathBuilder : String = ""
    if( configObject.getScheme.equals(HdfsConfiguration.Scheme.FILE)) {
      outPathBuilder = configObject.getPath + "/" + configObject.getWriterPath
    }
    else if( configObject.getScheme.equals(HdfsConfiguration.Scheme.HDFS)) {
      outPathBuilder = configObject.getScheme + "://" + configObject.getHost + ":" + configObject.getPort + "/" + configObject.getPath + "/" + configObject.getWriterPath
    }
    else if( configObject.getScheme.toString.equals("s3")) {
      outPathBuilder = configObject.getScheme + "://" + configObject.getPath + "/" + configObject.getWriterPath
    } else {
      throw new Exception("output scheme not recognized: " + configObject.getScheme)
    }
    outPathBuilder
  }

  def toProviderId(input : String) : String = {
    if( input.startsWith("@") )
      return input.substring(1)
    if( input.contains(':'))
      input.substring(input.lastIndexOf(':')+1)
    else input
  }

}