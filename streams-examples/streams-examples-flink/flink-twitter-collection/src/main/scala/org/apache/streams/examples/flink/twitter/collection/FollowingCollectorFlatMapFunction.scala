package org.apache.streams.examples.flink.twitter.collection

import java.util.concurrent.TimeUnit

import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.api.common.accumulators.IntCounter
import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.util.Collector
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfiguration
import org.apache.streams.core.StreamsDatum
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline.toProviderId
import org.apache.streams.flink.StreamsFlinkConfiguration
import org.apache.streams.twitter.config.TwitterFollowingConfiguration
import org.apache.streams.twitter.provider.TwitterFollowingProvider

import scala.collection.JavaConversions._

class FollowingCollectorFlatMapFunction(
                                         streamsConfiguration : StreamsConfiguration,
                                         twitterConfiguration : TwitterFollowingConfiguration = new ComponentConfigurator(classOf[TwitterFollowingConfiguration]).detectConfiguration(),
                                         flinkConfiguration : StreamsFlinkConfiguration = new ComponentConfigurator(classOf[StreamsFlinkConfiguration]).detectConfiguration()
                                       ) extends RichFlatMapFunction[String, StreamsDatum] with Serializable {

  var size : IntCounter = new IntCounter()
  var counter : IntCounter = new IntCounter()

  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("FlinkTwitterFollowingPipeline.size", this.size)
    getRuntimeContext().addAccumulator("FlinkTwitterFollowingPipeline.counter", this.counter)
  }

  override def flatMap(input: String, out: Collector[StreamsDatum]): Unit = {
    size.add(input.size)
    collectConnections(input, out)
  }

  def collectConnections(id : String, out : Collector[StreamsDatum]) = {
    val conf = twitterConfiguration.withInfo(List(toProviderId(id))).asInstanceOf[TwitterFollowingConfiguration]
    val twitProvider: TwitterFollowingProvider = new TwitterFollowingProvider(conf)
    twitProvider.prepare(twitProvider)
    twitProvider.startStream()
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
      val current = twitProvider.readCurrent().iterator().toList
      counter.add(current.size)
      current.map(out.collect(_))
    } while( twitProvider.isRunning )
  }

}
