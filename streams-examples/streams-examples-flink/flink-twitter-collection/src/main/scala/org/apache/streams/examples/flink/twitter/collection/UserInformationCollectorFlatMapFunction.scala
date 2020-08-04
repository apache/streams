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
import org.apache.streams.twitter.config.TwitterUserInformationConfiguration
import org.apache.streams.twitter.provider.TwitterUserInformationProvider

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 6/2/16.
  */
class UserInformationCollectorFlatMapFunction(
                                               streamsConfiguration : StreamsConfiguration,
                                               twitterConfiguration : TwitterUserInformationConfiguration,
                                               streamsFlinkConfiguration : StreamsFlinkConfiguration
                                             ) extends RichFlatMapFunction[List[String], StreamsDatum] with Serializable {
  var size : IntCounter = new IntCounter()
  var counter : IntCounter = new IntCounter()
  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("UserInformationCollectorFlatMapFunction.size", this.size)
    getRuntimeContext().addAccumulator("UserInformationCollectorFlatMapFunction.counter", this.counter)
  }
  override def flatMap(input: List[String], out: Collector[StreamsDatum]): Unit = {
    size.add(input.size)
    collectProfiles(input, out)
  }
  def collectProfiles(ids : List[String], out : Collector[StreamsDatum]) = {
    val conf = twitterConfiguration.withInfo(ids)
    val twitProvider: TwitterUserInformationProvider = new TwitterUserInformationProvider(conf)
    twitProvider.prepare(conf)
    twitProvider.startStream()
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
      val current = twitProvider.readCurrent().iterator().toList
      counter.add(current.size)
      current.map(out.collect(_))
    } while( twitProvider.isRunning )
  }
}
