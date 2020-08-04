package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.api.common.accumulators.IntCounter
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.apache.streams.config.StreamsConfiguration
import org.apache.streams.core.StreamsDatum
import org.apache.streams.flink.StreamsFlinkConfiguration
import org.apache.streams.twitter.config.TwitterTimelineProviderConfiguration
import org.apache.streams.twitter.provider.TwitterTimelineProvider

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 6/2/16.
  */
class TimelineCollectorFlatMapFunction(
                                        streamsConfiguration : StreamsConfiguration,
                                        twitterConfiguration : TwitterTimelineProviderConfiguration,
                                        streamsFlinkConfiguration : StreamsFlinkConfiguration
                                      ) extends RichFlatMapFunction[List[String], StreamsDatum] with Serializable {
  var size : IntCounter = new IntCounter()
  var counter : IntCounter = new IntCounter()
  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("TimelineCollectorFlatMapFunction.size", this.size)
    getRuntimeContext().addAccumulator("TimelineCollectorFlatMapFunction.counter", this.counter)
  }
  override def flatMap(input: List[String], out: Collector[StreamsDatum]): Unit = {
    size.add(input.size)
    collectPosts(input, out)
  }
  def collectPosts(ids : List[String], out : Collector[StreamsDatum]) = {
    try {
      val conf = twitterConfiguration.withInfo(ids).asInstanceOf[TwitterTimelineProviderConfiguration]
      val twitProvider: TwitterTimelineProvider = new TwitterTimelineProvider(conf)
      twitProvider.prepare(conf)
      twitProvider.startStream()
      do {
        Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
        val current = twitProvider.readCurrent().iterator().toList
        counter.add(current.size)
        current.map(out.collect(_))
      } while( twitProvider.isRunning )
    } finally {

    }
  }
}
