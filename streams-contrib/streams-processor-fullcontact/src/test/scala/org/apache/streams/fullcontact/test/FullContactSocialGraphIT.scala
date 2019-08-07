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

package org.apache.streams.fullcontact.test

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.fullcontact.FullContactSocialGraph
import org.apache.streams.fullcontact.PersonEnrichmentProcessor
import org.slf4j.{Logger, LoggerFactory}
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.AfterGroups
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

import scala.util.{Failure, Success, Try}

@AfterGroups(Array("PersonEnrichmentProcessorIT"))
class FullContactSocialGraphIT() {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FullContactSocialGraphIT])

  private val configfile = "target/test-classes/FullContactSocialGraphIT/FullContactSocialGraphIT.conf"

  @BeforeClass(alwaysRun = true)
  @throws[SkipException]
  def setup(): Unit = {
    try {
      val conf = new File(configfile)
      Assert.assertTrue(conf.exists)
      Assert.assertTrue(conf.canRead)
      Assert.assertTrue(conf.isFile)
      StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf))
      StreamsConfigurator.getConfig.getConfig("org.apache.streams.fullcontact.config.FullContactConfiguration")
      StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.config.FullContactConfiguration.token")
    } catch {
      case e : Throwable => throw new SkipException("Skipping FullContactSocialGraphIT because no api.fullcontact.com token has been provided", e)
    }
  }

  @Test(groups = Array("FullContactSocialGraphIT"), priority = 10)
  def fullContactSocialGraphIT : Unit = {

    val inputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.FullContactSocialGraph.input")
    val outputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.FullContactSocialGraph.output")

    Assert.assertTrue(Files.exists(Paths.get(inputFile)))

    val inputFileStream = new FileInputStream(new File(inputFile))
    val outputFileStream = new FileOutputStream(new File(outputFile), false)

    val job = new FullContactSocialGraph(inputFileStream, outputFileStream)
    val stats = job.call()

    LOGGER.info("stats: ", stats)

    Assert.assertTrue(stats.inputLines > 0)
    Assert.assertTrue(stats.personSummaries > 0)
    Assert.assertTrue(stats.allOrganizations > 0)
    Assert.assertTrue(stats.allInterestItems > 0)
    Assert.assertTrue(stats.uniqueInterests > 0)
    Assert.assertTrue(stats.topicHierarchy > 0)
    Assert.assertTrue(stats.allProfiles > 0)
    Assert.assertTrue(stats.allProfileRelationships > 0)
    Assert.assertTrue(stats.allEmploymentItems > 0)
    Assert.assertTrue(stats.uniqueEmployers > 0)
    Assert.assertTrue(stats.allUrlRelationships > 0)
    Assert.assertTrue(stats.allImageRelationships > 0)

  }

}