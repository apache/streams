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
import java.nio.file.Files
import java.nio.file.Paths

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.examples.flink.twitter.TwitterSpritzerPipelineConfiguration
import org.apache.streams.examples.flink.twitter.collection.FlinkTwitterSpritzerPipeline
import org.scalatest.Assertions._
import org.scalatest.concurrent.Eventually._
import org.scalatest.time.SpanSugar._
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.annotations.Test

import scala.io.Source

/**
  * FlinkTwitterSpritzerPipelineIT is an integration test for FlinkTwitterSpritzerPipeline.
  */
class FlinkTwitterSpritzerPipelineIT {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FlinkTwitterSpritzerPipelineIT])

  import FlinkTwitterSpritzerPipeline._

  @Test
  def flinkTwitterSpritzerPipelineIT = {

    val reference: Config = ConfigFactory.load()
    val conf_file: File = new File("target/test-classes/FlinkTwitterSpritzerPipelineIT.conf")
    assert(conf_file.exists())

    val testResourceConfig = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults.setAllowMissing(false))
    StreamsConfigurator.addConfig(testResourceConfig)

    val testConfig = new StreamsConfigurator(classOf[TwitterSpritzerPipelineConfiguration]).detectCustomConfiguration()

    setup(testConfig)

    val job = new FlinkTwitterSpritzerPipeline(config = testConfig)
    val jobThread = new Thread(job)
    jobThread.start
    jobThread.join(30000)
    job.stop()

    eventually (timeout(60 seconds), interval(1 seconds)) {
      assert(Files.exists(Paths.get(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath)))
      val lines = Source.fromFile(testConfig.getDestination.getPath + "/" + testConfig.getDestination.getWriterPath, "UTF-8").getLines.toList
      assert(lines.size > 10)
      lines foreach {
        line => assert( line.contains("created_at") )
      }
    }

  }

}
