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
import org.apache.streams.examples.flink.FlinkBase.toProviderId
import org.apache.streams.flink.StreamsFlinkConfiguration
import org.apache.streams.twitter.config.TwitterFollowingConfiguration
import org.apache.streams.twitter.pojo.Follow
import org.apache.streams.twitter.provider.TwitterFollowingProvider

import scala.collection.JavaConversions._

class FollowingCollectorFlatMapFunction(
                                         streamsConfiguration : StreamsConfiguration,
                                         twitterConfiguration : TwitterFollowingConfiguration = new ComponentConfigurator(classOf[TwitterFollowingConfiguration]).detectConfiguration(),
                                         flinkConfiguration : StreamsFlinkConfiguration = new ComponentConfigurator(classOf[StreamsFlinkConfiguration]).detectConfiguration()
                                       ) extends RichFlatMapFunction[List[String], Follow] with Serializable {

  var userids : IntCounter = new IntCounter()
  var follows : IntCounter = new IntCounter()

  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("FlinkTwitterFollowingPipeline.userids", this.userids)
    getRuntimeContext().addAccumulator("FlinkTwitterFollowingPipeline.follows", this.follows)
  }

  override def flatMap(input: List[String], out: Collector[Follow]): Unit = {
    userids.add(input.size)
    collectConnections(input, out)
  }

  def collectConnections(ids : List[String], out : Collector[Follow]) = {
    val conf = twitterConfiguration.withInfo(ids.map(toProviderId(_)))
    val twitProvider: TwitterFollowingProvider = new TwitterFollowingProvider(conf)
    twitProvider.prepare(twitProvider)
    twitProvider.startStream()
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
      val current : List[StreamsDatum] = twitProvider.readCurrent().iterator().toList
      follows.add(current.size)
      for( datum <- current ) {
        out.collect(datum.getDocument().asInstanceOf[Follow])
      }
    } while( twitProvider.isRunning )
  }

}
