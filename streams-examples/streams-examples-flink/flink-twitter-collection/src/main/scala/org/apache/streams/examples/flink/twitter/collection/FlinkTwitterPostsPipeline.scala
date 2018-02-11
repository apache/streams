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
import org.apache.streams.examples.flink.twitter.TwitterPostsPipelineConfiguration
import org.apache.streams.flink.FlinkStreamingConfiguration
import org.apache.streams.hdfs.{HdfsReaderConfiguration, HdfsWriterConfiguration}
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.pojo.Tweet
import org.apache.streams.twitter.provider.TwitterTimelineProvider
import org.hamcrest.MatcherAssert
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._

/**
  * FlinkTwitterPostsPipeline collects recent posts from all profiles from a
  * set of IDs, writing each post as a twitter:status in json format to dfs.
  */
object FlinkTwitterPostsPipeline extends FlinkBase {

  val STREAMS_ID: String = "FlinkTwitterPostsPipeline"

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterPostsPipeline])
  private val MAPPER: ObjectMapper = StreamsJacksonMapper.getInstance()

  override def main(args: Array[String]) = {
    super.main(args)
    val jobConfig = new ComponentConfigurator[TwitterPostsPipelineConfiguration](classOf[TwitterPostsPipelineConfiguration]).detectConfiguration(typesafe)
    if( !setup(jobConfig) ) System.exit(1)
    val pipeline: FlinkTwitterPostsPipeline = new FlinkTwitterPostsPipeline(jobConfig)
    val thread = new Thread(pipeline)
    thread.start()
    thread.join()
  }

  def setup(jobConfig: TwitterPostsPipelineConfiguration): Boolean =  {

    LOGGER.info("TwitterPostsPipelineConfiguration: " + jobConfig)

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

class FlinkTwitterPostsPipeline(config: TwitterPostsPipelineConfiguration = new ComponentConfigurator(classOf[TwitterPostsPipelineConfiguration]).detectConfiguration()) extends Runnable with java.io.Serializable {

  import FlinkTwitterPostsPipeline._

  override def run(): Unit = {

    val env: StreamExecutionEnvironment = streamEnvironment(MAPPER.convertValue(config, classOf[FlinkStreamingConfiguration]))

    env.setStreamTimeCharacteristic(TimeCharacteristic.IngestionTime)
    env.setNumberOfExecutionRetries(0)

    val inPath = buildReaderPath(new ComponentConfigurator(classOf[HdfsReaderConfiguration]).detectConfiguration())

    val outPath = buildWriterPath(new ComponentConfigurator(classOf[HdfsWriterConfiguration]).detectConfiguration())

    val ids: DataStream[String] = env.readTextFile(inPath).setParallelism(10).name("ids")

    val keyed_ids: KeyedStream[String, Int] = env.readTextFile(inPath).setParallelism(10).name("keyed_ids").keyBy( id => (id.hashCode % 100).abs )

    // these datums contain 'Tweet' objects
    val tweetDatums: DataStream[StreamsDatum] =
      keyed_ids.flatMap(new postCollectorFlatMapFunction).setParallelism(10).name("tweetDatums")

    val tweets: DataStream[Tweet] = tweetDatums
      .map(datum => datum.getDocument.asInstanceOf[Tweet]).name("tweets")

    val jsons: DataStream[String] = tweets
      .map(tweet => {
        val MAPPER = StreamsJacksonMapper.getInstance
        MAPPER.writeValueAsString(tweet)
      }).name("json")

    if( config.getTest == false )
      jsons.addSink(new BucketingSink[String](outPath)).setParallelism(3).name("hdfs")
    else
      jsons.writeAsText(outPath,FileSystem.WriteMode.OVERWRITE)
        .setParallelism(env.getParallelism)

    // if( test == true ) jsons.print();

    env.execute(STREAMS_ID)
  }

  class postCollectorFlatMapFunction extends RichFlatMapFunction[String, StreamsDatum] with Serializable {
    override def flatMap(input: String, out: Collector[StreamsDatum]): Unit = {
      collectPosts(input, out)
    }
    def collectPosts(id : String, out : Collector[StreamsDatum]) = {
      val twitterConfiguration = config.getTwitter
      twitterConfiguration.setInfo(List(toProviderId(id)))
      val twitProvider: TwitterTimelineProvider =
        new TwitterTimelineProvider(twitterConfiguration)
      twitProvider.prepare(twitProvider)
      twitProvider.startStream()
      var iterator: Iterator[StreamsDatum] = null
      do {
        Uninterruptibles.sleepUninterruptibly(config.getProviderWaitMs, TimeUnit.MILLISECONDS)
        twitProvider.readCurrent().iterator().toList.map(out.collect(_))
      } while( twitProvider.isRunning )
    }
  }


}
