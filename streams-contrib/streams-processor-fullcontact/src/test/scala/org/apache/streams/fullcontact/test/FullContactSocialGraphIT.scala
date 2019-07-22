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
import org.testng.annotations.{BeforeClass, Test}

import scala.util.{Failure, Success, Try}

class FullContactSocialGraphIT() {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[FullContactSocialGraphIT])

  private val configfile = "target/test-classes/FullContactSocialGraphIT/FullContactSocialGraphIT.conf"

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
  def fullContactSocialGraphIT : Unit = {

    val inputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.FullContactSocialGraph.input")
    val outputFile = StreamsConfigurator.getConfig.getString("org.apache.streams.fullcontact.FullContactSocialGraph.output")

    Assert.assertTrue(Files.exists(Paths.get(inputFile)))
    Assert.assertFalse(Files.exists(Paths.get(outputFile)))

    val inputFileStream = new FileInputStream(new File(inputFile))
    val outputFileStream = new FileOutputStream(new File(outputFile))

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