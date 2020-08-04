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

package org.apache.streams.examples.flink.twitter.test

import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.examples.flink.twitter.TwitterFollowingPipelineConfiguration
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterFollowingPipeline
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test

import scala.io.Source

/**
  * FlinkTwitterFollowingPipelineFollowersIT is an integration test for FlinkTwitterFollowingPipeline.
  */
class FlinkTwitterFollowingPipelineFollowersIT {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterFollowingPipelineFollowersIT])

  import FlinkTwitterFollowingPipeline._

  @Test
  def flinkTwitterFollowersPipelineFollowersIT = {

    val reference: Config = ConfigFactory.load()
    val conf_file: File = new File("target/test-classes/FlinkTwitterFollowingPipelineFollowersIT.conf")
    assert(conf_file.exists())

    val testResourceConfig = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults.setAllowMissing(false))
    StreamsConfigurator.addConfig(testResourceConfig)

    val testConfig = new StreamsConfigurator(classOf[TwitterFollowingPipelineConfiguration]).detectCustomConfiguration()

    setup(testConfig)

    val job = new FlinkTwitterFollowingPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(60 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath)))
      assert(
        Source.fromFile(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath, "UTF-8").getLines.size
          > 4000)
    }

  }

}
