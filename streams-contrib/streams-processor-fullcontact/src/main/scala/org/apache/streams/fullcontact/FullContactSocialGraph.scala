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

package org.apache.streams.fullcontact

import java.io.InputStream
import java.io.OutputStream
import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream, PrintStream}
import java.util.Scanner
import java.util.concurrent.Callable

import com.google.common.base.Preconditions
import com.typesafe.config.Config
import org.apache.juneau.json.JsonParser
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.fullcontact.FullContactSocialGraph.FullContactSocialGraphStats
import org.apache.streams.fullcontact.FullContactSocialGraph.typesafe
import org.apache.streams.fullcontact.pojo.PersonSummary
import org.apache.streams.fullcontact.util.FullContactUtils

import scala.io.Source
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/**
  * Produce an activity streams 2.0 social graph from full contact response data file.
  */
object FullContactSocialGraph {

  lazy val typesafe: Config = StreamsConfigurator.getConfig.getConfig("org.apache.streams.fullcontact.FullContactSocialGraph")

  /**
    * To use from command line:
    *
    * <p></p>
    * java -cp streams-dist-jar-with-dependencies.jar -Dconfig.file=application.conf org.apache.streams.fullcontact.SocialGraphCli
    *
    * <p></p>
    * Input stream should contain a series of json-serialized `PersonSummary` objects.
    *
    * <p></p>
    * Output stream will contain a TTL-serialized social graph.
    *
    * <p></p>
    * Input to the process is:
    *   A file if application.conf contains an 'input' key
    *   A file if -Dinput= is specified
    *   stdin otherwise
    *
    * Output from the process is:
    *   A file if application.conf contains an 'input' key
    *   A file if -Doutput= is specified
    *   stdout otherwise
    *
    * @link org.apache.streams.fullcontact.FullContactSocialGraph
    * @throws Exception Exception
    */
  @throws[Exception]
  final def main(args: Array[String]): Unit = {

    val inputStream: InputStream = if (typesafe.hasPath("input")) {
      new FileInputStream(new File(typesafe.getString("input")))
    } else System.in

    val outputStream: OutputStream = if (typesafe.hasPath("output")) {
      new FileOutputStream(new File(typesafe.getString("output")))
    } else System.out

    val job = Try(new FullContactSocialGraph(inputStream, outputStream))

    job match {
      case Success(_) => return job.get
      case Failure(t : Throwable) => throw new Exception(t)
    }
  }

  case class FullContactSocialGraphStats(
                                          inputLines : Int,
                                          personSummaries : Int,
                                          allOrganizations : Int,
                                          allInterestItems : Int,
                                          uniqueInterests : Int,
                                          topicHierarchy : Int,
                                          allProfiles : Int,
                                          allProfileRelationships : Int,
                                          allEmploymentItems : Int,
                                          uniqueEmployers : Int,
                                          allUrlRelationships : Int,
                                          allImageRelationships : Int
  )
}

class FullContactSocialGraph(in: InputStream, out: OutputStream ) extends Callable[FullContactSocialGraphStats] {

  val inputStream: BufferedInputStream = new BufferedInputStream(in)
  val outputStream: PrintStream = new PrintStream(out)

  override def call() : FullContactSocialGraphStats = {

    val input = Source.fromInputStream (inputStream)
    val inputLines = input.getLines().toSeq

    // sequence of all PersonSummary
    val personSummaries = inputLines.map (JsonParser.DEFAULT.parse (_, classOf[PersonSummary] ) ).toSeq

    // PersonSummary derived sequences
    val allOrganizations = FullContactUtils.allOrganizationItems (personSummaries.toIterator).toSeq
    val allInterestItems = FullContactUtils.allInterestItems (personSummaries.toIterator).toSeq
    val uniqueInterests = FullContactUtils.uniqueInterests (allInterestItems.toIterator).toSeq
    val topicHierarchy = FullContactUtils.topicHierarchy (allInterestItems.toIterator).toSeq

    val allProfiles = FullContactUtils.allProfiles (personSummaries.toIterator).toSeq
    val allProfileRelationships = FullContactUtils.allProfileRelationships (personSummaries.toIterator).toSeq

    val allEmploymentItems = FullContactUtils.allEmploymentItems (personSummaries.toIterator).toSeq
    val uniqueEmployers = FullContactUtils.uniqueEmployers (allEmploymentItems.toIterator).toSeq

    val allUrlRelationships = FullContactUtils.allUrlRelationships (personSummaries.toIterator).toSeq
    val allImageRelationships = FullContactUtils.allImageRelationships (personSummaries.toIterator).toSeq

    personSummaries.flatMap (FullContactUtils.safe_personSummaryAsTurtle).foreach (outputStream.println (_) )
    allOrganizations.flatMap (FullContactUtils.safe_organizationAsTurtle).foreach (outputStream.println (_) )

    uniqueInterests.flatMap (FullContactUtils.safe_interestTopicAsTurtle).foreach (outputStream.println (_) )
    topicHierarchy.map (FullContactUtils.topicRelationshipAsTurtle).foreach (outputStream.println (_) )

    allUrlRelationships.flatMap (FullContactUtils.safe_urlRelationshipAsTurtle).foreach (outputStream.println (_) )
    allImageRelationships.flatMap (FullContactUtils.safe_imageRelationshipAsTurtle).foreach (outputStream.println (_) )

    allProfiles.flatMap (FullContactUtils.safe_profileAsTurtle).foreach (outputStream.println (_) )
    allProfileRelationships.flatMap (FullContactUtils.safe_SocialProfileRelationshipAsTurtle).foreach (outputStream.println (_) )

    FullContactSocialGraphStats(
      inputLines = inputLines.size,
      personSummaries = personSummaries.size,
      allOrganizations = allOrganizations.size,
      allInterestItems = allInterestItems.size,
      uniqueInterests = uniqueInterests.size,
      topicHierarchy = topicHierarchy.size,
      allProfiles = allProfiles.size,
      allProfileRelationships = allProfileRelationships.size,
      allEmploymentItems = allEmploymentItems.size,
      uniqueEmployers = uniqueEmployers.size,
      allUrlRelationships = allUrlRelationships.size,
      allImageRelationships = allImageRelationships.size
    )
  }
}
