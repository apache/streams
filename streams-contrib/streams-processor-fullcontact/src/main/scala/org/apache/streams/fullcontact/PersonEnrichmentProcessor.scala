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

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.Scanner

import com.typesafe.config.Config
import org.apache.streams.config.ComponentConfigurator
import org.apache.streams.config.StreamsConfigurator
import org.apache.streams.core.StreamsDatum
import org.apache.streams.core.StreamsProcessor
import org.apache.streams.fullcontact.api.EnrichPersonRequest
import org.apache.streams.fullcontact.config.FullContactConfiguration
import org.apache.streams.fullcontact.pojo.PersonSummary

import scala.collection.JavaConversions._
import scala.io.Source
import scala.util.Failure
import scala.util.Success
import scala.util.Try

/**
  * Call enrich persons on a series of requests
  */
object PersonEnrichmentProcessor {

  lazy val typesafe: Config = StreamsConfigurator.getConfig.getConfig("org.apache.streams.fullcontact.PersonEnrichmentProcessor")

  /**
    * To use from command line:
    *
    * <p/>
    * Supply (at least) the following required configuration in application.conf:
    *
    * <p/>
    * org.apache.streams.fullcontact.config.FullContactConfiguration.token = ""
    *
    * <p/>
    * Launch syntax:
    *
    * <p/>
    * java -cp streams-dist-jar-with-dependencies.jar -Dconfig.file=./application.conf org.apache.streams.fullcontact.provider.PersonEnrichmentProcessor
    *
    * <p/>
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
    * @link org.apache.streams.fullcontact.api.EnrichPersonRequest
    * @param args application.conf input.jsonl output.jsonl
    * @throws Exception Exception
    */
  @throws[Exception]
  final def main(args: Array[String]): Unit = {

    val inputStream = if (typesafe.hasPath("input")) {
      new BufferedInputStream(new FileInputStream(new File(typesafe.getString("input"))))
    } else System.in

    val outputStream = if (typesafe.hasPath("output")) {
      new PrintStream(new FileOutputStream(new File(typesafe.getString("output"))))
    } else System.out

    val input = Source.fromInputStream(inputStream)
    val inputLines = input.getLines()
    val inputDatums = inputLines.map(entry => new StreamsDatum(entry))

    val outStream = new PrintStream(new BufferedOutputStream(outputStream))

    val outputDatums = streamDatums(inputDatums)
    val outputLines = outputDatums.map(_.getDocument().asInstanceOf[String])

    for( line <- outputLines ) {
      outStream.println(line)
    }
    outStream.flush()
    outStream.close()
  }

  def stream( iter : Iterator[EnrichPersonRequest] )
             ( implicit processor : PersonEnrichment = FullContact.getInstance() ) : Iterator[PersonSummary] = {
    iter.map( item => processor.enrichPerson(item) )
  }

  def streamDatums( iter : Iterator[StreamsDatum] )
            ( implicit processor : PersonEnrichmentProcessor = new PersonEnrichmentProcessor() ) : Iterator[StreamsDatum] = {
    iter.flatMap( item => processor.process(item) )
  }

  def processor : Iterator[EnrichPersonRequest] => Iterator[PersonSummary] = {
    stream(_)
  }

}

class PersonEnrichmentProcessor(config : FullContactConfiguration = new ComponentConfigurator[FullContactConfiguration](classOf[FullContactConfiguration]).detectConfiguration())
  extends StreamsProcessor with Serializable {

  var personEnrichment = FullContact.getInstance(config)

  /**
    * Process/Analyze the {@link org.apache.streams.core.StreamsDatum} and return the the StreamsDatums that will
    * passed to every down stream operation that reads from this processor.
    *
    * @param entry StreamsDatum to be processed
    * @return resulting StreamDatums from processing. Should never be null or contain null object.  Empty list OK.
    */
  override def process(entry: StreamsDatum): java.util.List[StreamsDatum] = {
    val request : EnrichPersonRequest = {
      entry.getDocument match {
        case _ : EnrichPersonRequest => entry.getDocument.asInstanceOf[EnrichPersonRequest]
        case _ : String => personEnrichment.parser.parse(entry.getDocument, classOf[EnrichPersonRequest])
        case _ => throw new Exception("invalid input type")
      }
    }
    val attempt = Try(personEnrichment.enrichPerson(request))
    attempt match {
      case Success(_ : PersonSummary) => List(new StreamsDatum(personEnrichment.serializer.serialize(attempt.get)))
      case Failure(_) => List()
    }
  }

  /**
    * Each operation must publish an identifier.
    */
  override def getId: String = "PersonEnrichmentProcessor"

  /**
    * This method will be called after initialization/serialization. Initialize any non-serializable objects here.
    *
    * @param configurationObject Any object to help intialize the operation. ie. Map, JobContext, Properties, etc. The type
    *                            will be based on where the operation is being run (ie. hadoop, storm, locally, etc.)
    */
  override def prepare(configurationObject: Any): Unit = {
    personEnrichment = FullContact.getInstance(configurationObject.asInstanceOf[FullContactConfiguration])
  }

  /**
    * No guarantee that this method will ever be called.  But upon shutdown of the stream, an attempt to call this method
    * will be made.
    * Use this method to terminate connections, etc.
    */
  override def cleanUp(): Unit = {
    personEnrichment.restClient.close()
    personEnrichment = null
  }
}
