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
import org.apache.streams.twitter.config.TwitterUserInformationConfiguration
import org.apache.streams.twitter.provider.TwitterUserInformationProvider
import org.apache.streams.twitter.pojo.User

import scala.collection.JavaConversions._

/**
  * Created by sblackmon on 6/2/16.
  */
class UserInformationCollectorFlatMapFunction(
                                               streamsConfiguration : StreamsConfiguration,
                                               twitterConfiguration : TwitterUserInformationConfiguration,
                                               streamsFlinkConfiguration : StreamsFlinkConfiguration
                                             ) extends RichFlatMapFunction[List[String], User] with Serializable {
  var ids : IntCounter = new IntCounter()
  var users : IntCounter = new IntCounter()
  override def open(parameters: Configuration): Unit = {
    getRuntimeContext().addAccumulator("UserInformationCollectorFlatMapFunction.ids", this.ids)
    getRuntimeContext().addAccumulator("UserInformationCollectorFlatMapFunction.users", this.users)
  }
  override def flatMap(input: List[String], out: Collector[User]): Unit = {
    ids.add(input.size)
    collectProfiles(input, out)
  }
  def collectProfiles(ids : List[String], out : Collector[User]) = {
    val conf = twitterConfiguration.withInfo(ids)
    val twitProvider: TwitterUserInformationProvider = new TwitterUserInformationProvider(conf)
    twitProvider.prepare(conf)
    twitProvider.startStream()
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getProviderWaitMs, TimeUnit.MILLISECONDS)
      val current : List[StreamsDatum] = twitProvider.readCurrent().iterator().toList
      users.add(current.size)
      for( datum <- current ) {
        out.collect(datum.getDocument().asInstanceOf[User])
      }
    } while( twitProvider.isRunning )
  }
}
