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

import java.io.Serializable
import java.util.Objects
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.JobExecutionResult
import org.apache.flink.api.common.serialization.SimpleStringEncoder
import org.apache.flink.api.scala._
import org.apache.flink.configuration.Configuration
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.source.RichSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.KeyedStream
import org.apache.flink.streaming.api.scala.DataStream
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterSpritzerPipelineConfiguration
import org.apache.streams.hdfs.HdfsWriterConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.config.TwitterStreamConfiguration
import org.apache.streams.twitter.converter.TwitterDateTimeFormat
import org.apache.streams.twitter.provider.TwitterStreamProvider
import org.hamcrest.MatcherAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import scala.collection.JavaConversions._

/**
  * FlinkTwitterSpritzerPipeline opens a spritzer stream and writes
  * each post received as a twitter:status in json format to dfs.
  */
object FlinkTwitterSpritzerPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterSpritzerPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterPostsPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator(classOf[TwitterSpritzerPipelineConfiguration]).detectConfiguration(typesafe)
    if( !setup(jobConfig) ) System.exit(1)
    val pipeline: FlinkTwitterSpritzerPipeline = new FlinkTwitterSpritzerPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterSpritzerPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterSpritzerPipelineConfiguration: " + jobConfig)

    if( jobConfig == null ) {
      LOGGER.error("jobConfig is null!")
      System.err.println("jobConfig is null!")
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

class FlinkTwitterSpritzerPipeline(config: TwitterSpritzerPipelineConfiguration = new StreamsConfigurator[TwitterSpritzerPipelineConfiguration](classOf[TwitterSpritzerPipelineConfiguration]).detectCustomConfiguration()) extends Runnable with java.io.Serializable {

  import FlinkTwitterSpritzerPipeline._

  val spritzerSource = new SpritzerSource(config.getTwitter)

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(config)

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)
    env.setNumberOfExecutionRetries(0)

    val outPath = buildWriterPath(new ComponentConfigurator(classOf[HdfsWriterConfiguration]).detectConfiguration())

    val jsons : DataStream[String] = env.addSource(spritzerSource)

    val keyed_jsons: KeyedStream[String, Int] = jsons.
      setParallelism(streamsConfig.getParallelism().toInt).
      keyBy( id => (id.hashCode % streamsConfig.getParallelism().toInt).abs )

    val fileSink : StreamingFileSink[String] = StreamingFileSink.
      forRowFormat(new Path(outPath), new SimpleStringEncoder[String]("UTF-8")).
      build()

    if( config.getTest == true ) {
      keyed_jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
    } else {
      keyed_jsons.addSink(fileSink)
    }

    val result: JobExecutionResult = env.execute("FlinkTwitterPostsPipeline")

    LOGGER.info("JobExecutionResult: {}", result.getJobExecutionResult)

    LOGGER.info("JobExecutionResult.getNetRuntime: {}", result.getNetRuntime())

    LOGGER.info("JobExecutionResult.getAllAccumulatorResults: {}", MAPPER.writeValueAsString(result.getAllAccumulatorResults()))

  }

  def stop(): Unit = {
    spritzerSource.cancel()
  }

  class SpritzerSource(sourceConfig: TwitterStreamConfiguration) extends RichSourceFunction[String] with Serializable /*with StoppableFunction*/ {

    var mapper: ObjectMapper = _

    var twitProvider: TwitterStreamProvider = _

    @throws[Exception]
    override def open(parameters: Configuration): Unit = {
      mapper = StreamsJacksonMapper.getInstance(TwitterDateTimeFormat.TWITTER_FORMAT)
      twitProvider = new TwitterStreamProvider( sourceConfig )
      twitProvider.prepare(twitProvider)
      twitProvider.startStream()
    }

    override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
      var iterator: Iterator[StreamsDatum] = null
      do {
        Uninterruptibles.sleepUninterruptibly(config.getProviderWaitMs, TimeUnit.MILLISECONDS)
        iterator = twitProvider.readCurrent().iterator()
        iterator.toList.map(datum => ctx.collect(mapper.writeValueAsString(datum.getDocument)))
      } while( twitProvider.isRunning )
    }

    override def cancel(): Unit = {
      close()
    }

//    override def stop(): Unit = {
//      close()
//    }
  }


}
