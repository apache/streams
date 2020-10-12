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
import org.apache.flink.streaming.api.scala.AllWindowedStream
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.FlinkBase.idListWindowFunction
import org.apache.streams.examples.flink.twitter.TwitterUserInformationPipelineConfiguration
import org.apache.streams.hdfs.HdfsReaderConfiguration
import org.apache.streams.hdfs.HdfsWriterConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.pojo.User
import org.hamcrest.MatcherAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * FlinkTwitterPostsPipeline collects the current user profile of a
  * set of IDs, writing each as a twitter:user in json format to dfs.
  */
object FlinkTwitterUserInformationPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterUserInformationPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    if( args.length > 0 ) {
      LOGGER.info("Args: {}", args)
      configUrl = args(0)
    }
    if( !setup(configUrl) ) System.exit(1)
    val jobConfig = new StreamsConfigurator(classOf[TwitterUserInformationPipelineConfiguration]).detectCustomConfiguration()
    if( !setup(jobConfig) ) System.exit(1)
    val pipeline: FlinkTwitterUserInformationPipeline = new FlinkTwitterUserInformationPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterUserInformationPipelineConfiguration): Boolean =  {

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

class FlinkTwitterUserInformationPipeline(config: TwitterUserInformationPipelineConfiguration = new StreamsConfigurator[TwitterUserInformationPipelineConfiguration](classOf[TwitterUserInformationPipelineConfiguration]).detectCustomConfiguration()) extends Runnable with java.io.Serializable {

  import FlinkTwitterUserInformationPipeline._
  //import FlinkBase.streamsConfig

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(config)

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(new ComponentConfigurator(classOf[HdfsReaderConfiguration]).detectConfiguration())

    val outPath = buildWriterPath(new ComponentConfigurator(classOf[HdfsWriterConfiguration]).detectConfiguration())

    val ids: DataStream[String] = env.readTextFile(inPath).setParallelism(env.getParallelism).name("ids")

    val idWindows: AllWindowedStream[String, GlobalWindow] = ids.countWindowAll(100)

    val idLists: DataStream[List[String]] = idWindows.apply[List[String]] (new idListWindowFunction()).name("idLists")

    val users: DataStream[User] = idLists.flatMap(new UserInformationCollectorFlatMapFunction(streamsConfig, config.getTwitter, streamsFlinkConfiguration)).setParallelism(env.getParallelism).name("users")

    val jsons: DataStream[String] = users
      .map(user => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(user)
      }).name("jsons")

    val fileSink : StreamingFileSink[String] = StreamingFileSink.
      forRowFormat(new Path(outPath), new SimpleStringEncoder[String]("UTF-8")).
      withRollingPolicy(rollingPolicy).
      withBucketAssigner(basePathBucketAssigner).build();

    if( config.getTest == true ) {
      jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
    } else {
      jsons.addSink(fileSink).name("fileSink")
    }

    val result: JobExecutionResult = env.execute("FlinkTwitterUserInformationPipeline")

    LOGGER.info("JobExecutionResult: {}", result.getJobExecutionResult)

    LOGGER.info("JobExecutionResult.getNetRuntime: {}", result.getNetRuntime())

    LOGGER.info("JobExecutionResult.getAllAccumulatorResults: {}", MAPPER.writeValueAsString(result.getAllAccumulatorResults()))


  }

}
