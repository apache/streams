package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.api.common.accumulators.IntCounter
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.apache.streams.config.StreamsConfiguration
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.FlinkBase.toProviderId
import org.apache.streams.flink.StreamsFlinkConfiguration
import org.apache.streams.twitter.config.TwitterTimelineProviderConfiguration
import org.apache.streams.twitter.pojo.Tweet
import org.apache.streams.twitter.provider.TwitterTimelineProvider

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 6/2/16.
  */
class TimelineCollectorFlatMapFunction(
                                        streamsConfiguration : StreamsConfiguration,
                                        twitterConfiguration : TwitterTimelineProviderConfiguration,
                                        streamsFlinkConfiguration : StreamsFlinkConfiguration
                                      ) extends RichFlatMapFunction[String, Tweet] with Serializable {
  var userids : IntCounter = new IntCounter()
  var posts : IntCounter = new IntCounter()
  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("TimelineCollectorFlatMapFunction.userids", this.userids)
    getRuntimeContext().addAccumulator("TimelineCollectorFlatMapFunction.posts", this.posts)
  }
  override def flatMap(input: String, out: Collector[Tweet]): Unit = {
    userids.add(input.size)
    collectPosts(input, out)
  }
  def collectPosts(id : String, out : Collector[Tweet]) = {
    try {
      val conf = twitterConfiguration.withInfo(List(toProviderId(id))).asInstanceOf[TwitterTimelineProviderConfiguration]
      val twitProvider: TwitterTimelineProvider = new TwitterTimelineProvider(conf)
      twitProvider.prepare(conf)
      twitProvider.startStream()
      do {
        Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
        val current : List[StreamsDatum] = twitProvider.readCurrent().iterator().toList
        posts.add(current.size)
        for( datum <- current ) {
          out.collect(datum.getDocument().asInstanceOf[Tweet])
        }
      } while( twitProvider.isRunning )
    } finally {

    }
  }
}
