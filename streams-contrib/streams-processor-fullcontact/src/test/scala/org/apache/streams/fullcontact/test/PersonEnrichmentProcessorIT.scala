package org.apache.streams.fullcontact.test

import java.io.File
import java.nio.file.{Files, Paths}

import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigValueFactory
import org.apache.streams.config.{ComponentConfigurator, StreamsConfigurator}
import org.apache.streams.fullcontact.PersonEnrichmentProcessor
import org.apache.streams.fullcontact.config.FullContactConfiguration
import org.slf4j.{Logger, LoggerFactory}
import org.testng.Assert
import org.testng.annotations.{BeforeClass, Test}

import scala.io.Source
import scala.util.{Failure, Success, Try}

class PersonEnrichmentProcessorIT() {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[PersonEnrichmentProcessorIT])

  private val configfile = "target/test-classes/PersonEnrichmentProcessorIT/PersonEnrichmentProcessorIT.conf"

  @BeforeClass(alwaysRun = true)
  @throws[Exception]
  def setup(): Unit = {
    val conf = new File(configfile)
    Assert.assertTrue(conf.exists)
    Assert.assertTrue(conf.canRead)
    Assert.assertTrue(conf.isFile)
    StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf))
  }

  @Test(priority = 10)
  def personEnrichmentProcessorIT : Unit = {

    val inputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.PersonEnrichmentProcessor.input")
    val outputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.PersonEnrichmentProcessor.output")

    Assert.assertTrue(Files.exists(Paths.get(inputFile)))
    Assert.assertFalse(Files.exists(Paths.get(outputFile)))

    StreamsConfigurator.addConfig(ConfigFactory.empty().withValue("org.apache.streams.fullcontact.PersonEnrichmentProcessor.input", ConfigValueFactory.fromAnyRef(inputFile)))
    StreamsConfigurator.addConfig(ConfigFactory.empty().withValue("org.apache.streams.fullcontact.PersonEnrichmentProcessor.output", ConfigValueFactory.fromAnyRef(outputFile)))

    val job = Try(PersonEnrichmentProcessor.main(List().toArray))

    job match {
      case Success(_) => return
      case Failure(t : Throwable) => throw new Exception(t)
    }

  }

}