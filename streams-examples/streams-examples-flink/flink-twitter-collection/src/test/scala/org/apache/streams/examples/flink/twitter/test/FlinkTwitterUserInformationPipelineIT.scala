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

package com.peoplepattern.streams.twitter.collection

import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions}
import org.apache.streams.config.{ComponentConfigurator, StreamsConfiguration, StreamsConfigurator}
import org.apache.streams.examples.flink.twitter.TwitterUserInformationPipelineConfiguration
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterUserInformationPipeline
import org.scalatest.FlatSpec
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._
import org.slf4j.{Logger, LoggerFactory}
import org.testng.annotations.Test

import scala.io.Source

/**
  * FlinkTwitterUserInformationPipelineIT is an integration test for FlinkTwitterUserInformationPipeline.
  */
class FlinkTwitterUserInformationPipelineIT extends FlatSpec {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterUserInformationPipelineIT])

  import FlinkTwitterUserInformationPipeline._

  @Test
  def flinkTwitterUserInformationPipelineIT = {

    val reference: Config = ConfigFactory.load()
    val conf_file: File = new File("target/test-classes/FlinkTwitterUserInformationPipelineIT.conf")
    assert(conf_file.exists())
    val testResourceConfig: Config = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));

    val typesafe: Config = testResourceConfig.withFallback(reference).resolve()
    val streams: StreamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe)
    val testConfig = new ComponentConfigurator(classOf[TwitterUserInformationPipelineConfiguration]).detectConfiguration(typesafe)

    setup(testConfig)

    val job = new FlinkTwitterUserInformationPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join

    eventually (timeout(30 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath)))
      assert(
        Source.fromFile(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath, "UTF-8").getLines.size
          > 500)
    }

  }

}
