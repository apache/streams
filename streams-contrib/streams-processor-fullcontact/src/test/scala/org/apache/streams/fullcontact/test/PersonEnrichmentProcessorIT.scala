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
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.fullcontact.PersonEnrichmentProcessor
import org.apache.streams.fullcontact.config.FullContactConfiguration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testng.Assert
import org.testng.SkipException
import org.testng.annotations.BeforeClass
import org.testng.annotations.BeforeGroups
import org.testng.annotations.Test

import scala.io.Source
import scala.util.{Failure, Success, Try}

@BeforeGroups(Array("FullContactSocialGraphIT"))
class PersonEnrichmentProcessorIT() {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[PersonEnrichmentProcessorIT])

  private val configfile = "target/test-classes/PersonEnrichmentProcessorIT/PersonEnrichmentProcessorIT.conf"

  @BeforeClass(alwaysRun = true)
  @throws[Exception]
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

  @Test(groups = Array("PersonEnrichmentProcessorIT"), priority = 10)
  def personEnrichmentProcessorIT : Unit = {

    val inputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.PersonEnrichmentProcessor.input")
    val outputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.PersonEnrichmentProcessor.output")

    Assert.assertTrue(Files.exists(Paths.get(inputFile)))

    StreamsConfigurator.addConfig(ConfigFactory.empty().withValue("org.apache.streams.fullcontact.PersonEnrichmentProcessor.input", ConfigValueFactory.fromAnyRef(inputFile)))
    StreamsConfigurator.addConfig(ConfigFactory.empty().withValue("org.apache.streams.fullcontact.PersonEnrichmentProcessor.output", ConfigValueFactory.fromAnyRef(outputFile)))

    val job = Try(PersonEnrichmentProcessor.main(List().toArray))

    job match {
      case Success(_) => return
      case Failure(t : Throwable) => throw new Exception(t)
    }

  }

}