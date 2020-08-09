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

package org.apache.streams.examples.flink.twitter.collection

import java.util.Objects

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.KeyedStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.pojo.Follow
import org.hamcrest.MatcherAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * FlinkTwitterFollowingPipeline collects friends or followers of all profiles from a
  * set of IDs, writing each connection as a twitter:follow in json format to dfs.
  */
object FlinkTwitterFollowingPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterFollowingPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    if( args.length > 0 ) {
      LOGGER.info("Args: {}", args)
      configUrl = args(0)
    }
    if( !setup(configUrl) ) System.exit(1)
    val jobConfig = new StreamsConfigurator(classOf[TwitterFollowingPipelineConfiguration]).detectCustomConfiguration()
    if( !setup(jobConfig) ) System.exit(1)
    val pipeline: FlinkTwitterFollowingPipeline = new FlinkTwitterFollowingPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterFollowingPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterFollowingPipelineConfiguration: " + jobConfig)

    if( jobConfig == null ) {
      LOGGER.error("jobConfig is null!")
      System.err.println("jobConfig is null!")
      return false
    }

    if( jobConfig.getSource == null ) {
      LOGGER.error("jobConfig.getSource is null!")
      System.err.println("jobConfig.getSource is null!")
      return false
    }

    if( jobConfig.getDestination == null ) {
      LOGGER.error("jobConfig.getDestination is null!")
      System.err.println("jobConfig.getDestination is null!")
      return false
    }

    if( jobConfig.getTwitter == null ) {
      LOGGER.error("jobConfig.getTwitter is null!")
      System.err.println("jobConfig.getTwitter is null!")
      return false
    }

    Objects.requireNonNull(jobConfig.getTwitter.getOauth)
    MatcherAssert.assertThat("OAuth Access Token is not Empty",
      StringUtils.isNotEmpty(jobConfig.getTwitter.getOauth.getAccessToken))
    MatcherAssert.assertThat("OAuth Access Secret is not Empty",
      StringUtils.isNotEmpty(jobConfig.getTwitter.getOauth.getAccessTokenSecret))
    MatcherAssert.assertThat("OAuth Consumer Key is not Empty",
      StringUtils.isNotEmpty(jobConfig.getTwitter.getOauth.getConsumerKey))
    MatcherAssert.assertThat("OAuth Consumer Secret is not Empty",
      StringUtils.isNotEmpty(jobConfig.getTwitter.getOauth.getConsumerSecret))

    true

  }

}

class FlinkTwitterFollowingPipeline(config: TwitterFollowingPipelineConfiguration = new StreamsConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectCustomConfiguration()) extends Runnable with java.io.Serializable {

  import FlinkTwitterFollowingPipeline._
  import FlinkTwitterFollowingPipeline.rollingPolicy

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(config)

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(config.getSource)

    val outPath = buildWriterPath(config.getDestination)

    val ids: DataStream[String] = env.readTextFile(inPath)

    val keyed_ids: KeyedStream[String, Int] = ids.
      name("keyed_ids").
      setParallelism(streamsConfig.getParallelism().toInt).
      keyBy( id => (id.hashCode % streamsConfig.getParallelism().toInt).abs )

    // these datums contain 'Follow' objects
    val followDatums: DataStream[StreamsDatum] = keyed_ids.
      flatMap(new FollowingCollectorFlatMapFunction(streamsConfig, config.getTwitter, streamsFlinkConfiguration)).
      name("followDatums").
      setParallelism(streamsConfig.getParallelism().toInt)

    val follows: DataStream[Follow] = followDatums.
      name("follows")
      .map(datum => datum.getDocument.asInstanceOf[Follow])

    val jsons: DataStream[String] = follows.
      name("jsons")
      .map(follow => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(follow)
      }).
      setParallelism(streamsConfig.getParallelism().toInt)

    val keyed_jsons: KeyedStream[String, Int] = jsons.
      name("keyed_jsons").
      setParallelism(streamsConfig.getParallelism().toInt).
      keyBy( id => (id.hashCode % streamsConfig.getParallelism().toInt).abs )

    val fileSink : StreamingFileSink[String] = StreamingFileSink.
      forRowFormat(new Path(outPath), new SimpleStringEncoder[String]("UTF-8")).
      withRollingPolicy(rollingPolicy).
      withBucketAssigner(basePathBucketAssigner).build();

    if( config.getTest == true ) {
      keyed_jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
    } else {
      keyed_jsons.addSink(fileSink).name("fileSink")
    }

    val result: JobExecutionResult = env.execute("FlinkTwitterFollowingPipeline")

    LOGGER.info("JobExecutionResult: {}", result.getJobExecutionResult)

    LOGGER.info("JobExecutionResult.getNetRuntime: {}", result.getNetRuntime())

    LOGGER.info("JobExecutionResult.getAllAccumulatorResults: {}", MAPPER.writeValueAsString(result.getAllAccumulatorResults()))

  }

}