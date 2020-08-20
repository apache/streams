package org.apache.streams.examples.flink.twitter.collection

import java.io.Serializable
import java.util.Objects
import java.util.concurrent.TimeUnit

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.Uninterruptibles
import org.apache.flink.configuration.Configuration
import org.apache.flink.api.common.accumulators.IntCounter
import org.apache.flink.streaming.api.functions.source.RichSourceFunction
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.core.StreamsDatum
import org.apache.streams.flink.StreamsFlinkConfiguration
import org.apache.streams.jackson.StreamsJacksonMapper
import org.apache.streams.twitter.config.TwitterStreamConfiguration
import org.apache.streams.twitter.converter.TwitterDateTimeFormat
import org.apache.streams.twitter.provider.TwitterStreamProvider

import scala.collection.JavaConversions._

class SpritzerSource(
                      streamsConfiguration : StreamsConfiguration,
                      twitterConfiguration : TwitterStreamConfiguration = new ComponentConfigurator(classOf[TwitterStreamConfiguration]).detectConfiguration(),
                      flinkConfiguration : StreamsFlinkConfiguration = new ComponentConfigurator(classOf[StreamsFlinkConfiguration]).detectConfiguration()
                    ) extends RichSourceFunction[String] with Serializable /*with StoppableFunction*/ {

  var mapper: ObjectMapper = _

  var twitProvider: TwitterStreamProvider = _

  var items : IntCounter = new IntCounter()

  @throws[Exception]
  override def open(parameters: Configuration): Unit = {
    mapper = StreamsJacksonMapper.getInstance(TwitterDateTimeFormat.TWITTER_FORMAT)
    getRuntimeContext().addAccumulator("SpritzerSource.items", this.items)
    twitProvider = new TwitterStreamProvider( twitterConfiguration )
    twitProvider.prepare(twitProvider)
    twitProvider.startStream()
  }

  override def run(ctx: SourceFunction.SourceContext[String]): Unit = {
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
      val current : List[StreamsDatum] = twitProvider.readCurrent().iterator().toList
      items.add(current.size)
      for( item <- current ) {
        ctx.collect(mapper.writeValueAsString(item.getDocument))
      }
    } while( twitProvider.isRunning )
  }

  override def cancel(): Unit = {
    close()
  }

}