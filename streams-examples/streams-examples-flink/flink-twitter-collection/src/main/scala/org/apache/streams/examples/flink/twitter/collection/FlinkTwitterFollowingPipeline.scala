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
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.commons.lang3.StringUtils
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.scala._
import org.apache.flink.core.fs.FileSystem
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.connectors.fs.bucketing.BucketingSink
import org.apache.flink.util.Collector
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase
import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import org.apache.streams.flink.{FlinkStreamingConfiguration, StreamsFlinkConfiguration}
import org.apache.streams.hdfs.{HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.config.TwitterFollowingConfiguration
import org.apache.streams.twitter.pojo.Follow
import org.apache.streams.twitter.provider.TwitterFollowingProvider
import org.hamcrest.MatcherAssert
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
  * FlinkTwitterFollowingPipeline collects friends or followers of all profiles from a
  * set of IDs, writing each connection as a twitter:follow in json format to dfs.
  */
object FlinkTwitterFollowingPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterFollowingPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator[TwitterFollowingPipelineConfiguration](classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration(typesafe)
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

class FlinkTwitterFollowingPipeline(config: TwitterFollowingPipelineConfiguration = new ComponentConfigurator(classOf[TwitterFollowingPipelineConfiguration]).detectConfiguration()) extends Runnable with java.io.Serializable {

  import FlinkTwitterFollowingPipeline._

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(new ComponentConfigurator(classOf[HdfsReaderConfiguration]).detectConfiguration())

    val outPath = buildWriterPath(new ComponentConfigurator(classOf[HdfsWriterConfiguration]).detectConfiguration())

    val keyed_ids: KeyedStream[String, Int] = env.readTextFile(inPath).setParallelism(10).keyBy( id => (id.hashCode % 100).abs )

    // these datums contain 'Follow' objects
    val followDatums: DataStream[StreamsDatum] =
      keyed_ids.flatMap(new FollowingCollectorFlatMapFunction(config.getTwitter)).setParallelism(10)

    val follows: DataStream[Follow] = followDatums
      .map(datum => datum.getDocument.asInstanceOf[Follow])

    val jsons: DataStream[String] = follows
      .map(follow => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(follow)
      })

    if( config.getTest == false )
      jsons.addSink(new BucketingSink[String](outPath)).setParallelism(3)
    else
      jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
        .setParallelism(env.getParallelism)

    // if( test == true ) jsons.print();

    env.execute(STREAMS_ID)
  }

  class FollowingCollectorFlatMapFunction(
                                           twitterConfiguration : TwitterFollowingConfiguration = new ComponentConfigurator(classOf[TwitterFollowingConfiguration]).detectConfiguration(),
                                           flinkConfiguration : StreamsFlinkConfiguration = new ComponentConfigurator(classOf[StreamsFlinkConfiguration]).detectConfiguration()
                                         ) extends RichFlatMapFunction[String, StreamsDatum] with Serializable {

    override def flatMap(input: String, out: Collector[StreamsDatum]): Unit = {
      collectConnections(input, out)
    }

    def collectConnections(id : String, out : Collector[StreamsDatum]) = {
      val twitProvider: TwitterFollowingProvider =
        new TwitterFollowingProvider(
          twitterConfiguration.withInfo(List(toProviderId(id))).asInstanceOf[TwitterFollowingConfiguration]
        )
      twitProvider.prepare(twitProvider)
      twitProvider.startStream()
      var iterator: Iterator[StreamsDatum] = null
      do {
        Uninterruptibles.sleepUninterruptibly(flinkConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
        twitProvider.readCurrent().iterator().toList.map(out.collect(_))
      } while( twitProvider.isRunning )
    }
  }

}