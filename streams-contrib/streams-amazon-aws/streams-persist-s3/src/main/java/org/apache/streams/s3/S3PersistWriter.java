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

package org.apache.streams.s3;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.LineReadWriteUtil;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCountable;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * S3PersistWriter writes documents to s3.
 */
public class S3PersistWriter implements StreamsPersistWriter, DatumStatusCountable {

  public static final String STREAMS_ID = "S3PersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(S3PersistWriter.class);

  private static final char DELIMITER = '\t';

  private ObjectMapper objectMapper;
  private AmazonS3Client amazonS3Client;
  private S3WriterConfiguration s3WriterConfiguration;
  private final List<String> writtenFiles = new ArrayList<String>();
  protected LineReadWriteUtil lineWriterUtil;

  private final AtomicLong totalBytesWritten = new AtomicLong();
  private AtomicLong bytesWrittenThisFile = new AtomicLong();

  private final AtomicInteger totalRecordsWritten = new AtomicInteger();
  private AtomicInteger fileLineCounter = new AtomicInteger();

  private static Map<String, String> objectMetaData = new HashMap<String, String>();

  static {
    objectMetaData.put("line[0]", "id");
    objectMetaData.put("line[1]", "timeStamp");
    objectMetaData.put("line[2]", "metaData");
    objectMetaData.put("line[3]", "document");
  }

  private OutputStreamWriter currentWriter = null;

  public AmazonS3Client getAmazonS3Client() {
    return this.amazonS3Client;
  }

  public S3WriterConfiguration getS3WriterConfiguration() {
    return this.s3WriterConfiguration;
  }

  public List<String> getWrittenFiles() {
    return this.writtenFiles;
  }

  public Map<String, String> getObjectMetaData() {
    return this.objectMetaData;
  }

  public ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  public void setObjectMapper(ObjectMapper mapper) {
    this.objectMapper = mapper;
  }

  public void setObjectMetaData(Map<String, String> val) {
    this.objectMetaData = val;
  }

  public S3PersistWriter() {
    this(new ComponentConfigurator<>(S3WriterConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("s3")));
  }

  public S3PersistWriter(S3WriterConfiguration s3WriterConfiguration) {
    this.s3WriterConfiguration = s3WriterConfiguration;
  }

  /**
   * Instantiator with a pre-existing amazonS3Client, this is used to help with re-use.
   * @param amazonS3Client
   * If you have an existing amazonS3Client, it wont' bother to create another one
   * @param s3WriterConfiguration
   * Configuration of the write paths and instructions are still required.
   */
  public S3PersistWriter(AmazonS3Client amazonS3Client, S3WriterConfiguration s3WriterConfiguration) {
    this.amazonS3Client = amazonS3Client;
    this.s3WriterConfiguration = s3WriterConfiguration;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum streamsDatum) {

    synchronized (this) {
      // Check to see if we need to reset the file that we are currently working with
      if (this.currentWriter == null || ( this.bytesWrittenThisFile.get()  >= (this.s3WriterConfiguration.getMaxFileSize() * 1024 * 1024))) {
        try {
          LOGGER.info("Resetting the file");
          this.currentWriter = resetFile();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }

      String line = lineWriterUtil.convertResultToString(streamsDatum);

      try {
        this.currentWriter.write(line);
      } catch (IOException ex) {
        ex.printStackTrace();
      }

      // add the bytes we've written
      int recordSize = line.getBytes().length;
      this.totalBytesWritten.addAndGet(recordSize);
      this.bytesWrittenThisFile.addAndGet(recordSize);

      // increment the record count
      this.totalRecordsWritten.incrementAndGet();
      this.fileLineCounter.incrementAndGet();
    }

  }

  /**
   * Reset File when it's time to create a new file.
   * @return OutputStreamWriter
   * @throws Exception Exception
   */
  public synchronized OutputStreamWriter resetFile() throws Exception {
    // this will keep it thread safe, so we don't create too many files
    if (this.fileLineCounter.get() == 0 && this.currentWriter != null) {
      return this.currentWriter;
    }

    closeAndDestroyWriter();

    // Create the path for where the file is going to live.
    try {
      // generate a file name
      String fileName = this.s3WriterConfiguration.getWriterFilePrefix()
          + (this.s3WriterConfiguration.getChunk() ? "/" : "-")
          + new Date().getTime()
          + ".tsv";

      // create the output stream
      OutputStream outputStream = new S3OutputStreamWrapper(this.amazonS3Client,
          this.s3WriterConfiguration.getBucket(),
          this.s3WriterConfiguration.getWriterPath(),
          fileName,
          this.objectMetaData);

      // reset the counter
      this.fileLineCounter = new AtomicInteger();
      this.bytesWrittenThisFile = new AtomicLong();

      // add this to the list of written files
      writtenFiles.add(this.s3WriterConfiguration.getWriterPath() + fileName);

      // Log that we are creating this file
      LOGGER.info("File Created: Bucket[{}] - {}", this.s3WriterConfiguration.getBucket(), this.s3WriterConfiguration.getWriterPath() + fileName);

      // return the output stream
      return new OutputStreamWriter(outputStream);
    } catch (Exception ex) {
      LOGGER.error(ex.getMessage());
      throw ex;
    }
  }

  private synchronized void closeAndDestroyWriter() {
    // if there is a current writer, we must close it first.
    if (this.currentWriter != null) {
      this.safeFlush(this.currentWriter);
      this.closeSafely(this.currentWriter);
      this.currentWriter = null;

      // Logging of information to alert the user to the activities of this class
      LOGGER.debug("File Closed: Records[{}] Bytes[{}] {} ", this.fileLineCounter.get(), this.bytesWrittenThisFile.get(), this.writtenFiles.get(this.writtenFiles.size() - 1));
    }
  }

  private synchronized void closeSafely(Writer writer)  {
    if (writer != null) {
      try {
        writer.flush();
        writer.close();
      } catch (Exception ex) {
        LOGGER.trace("closeSafely", ex);
      }
      LOGGER.debug("File Closed");
    }
  }

  private void safeFlush(Flushable flushable) {
    // This is wrapped with a ByteArrayOutputStream, so this is really safe.
    if (flushable != null) {
      try {
        flushable.flush();
      } catch (IOException ex) {
        LOGGER.trace("safeFlush", ex);
      }
    }
  }

  @Override
  public void prepare(Object configurationObject) {

    lineWriterUtil = LineReadWriteUtil.getInstance(s3WriterConfiguration);

    // Connect to S3
    synchronized (this) {

      try {
        // if the user has chosen to not set the object mapper, then set a default object mapper for them.
        if (this.objectMapper == null) {
          this.objectMapper = StreamsJacksonMapper.getInstance();
        }

        // Create the credentials Object
        if (this.amazonS3Client == null) {
          AWSCredentials credentials = new BasicAWSCredentials(s3WriterConfiguration.getKey(), s3WriterConfiguration.getSecretKey());

          ClientConfiguration clientConfig = new ClientConfiguration();
          clientConfig.setProtocol(Protocol.valueOf(s3WriterConfiguration.getProtocol().toString()));

          // We do not want path style access
          S3ClientOptions clientOptions = new S3ClientOptions();
          clientOptions.setPathStyleAccess(false);

          this.amazonS3Client = new AmazonS3Client(credentials, clientConfig);
          if (!Strings.isNullOrEmpty(s3WriterConfiguration.getRegion())) {
            this.amazonS3Client.setRegion(Region.getRegion(Regions.fromName(s3WriterConfiguration.getRegion())));
          }
          this.amazonS3Client.setS3ClientOptions(clientOptions);
        }
      } catch (Exception ex) {
        LOGGER.error("Exception while preparing the S3 client: {}", ex);
      }

      Preconditions.checkArgument(this.amazonS3Client != null);
    }
  }

  public void cleanUp() {
    closeAndDestroyWriter();
  }

  @Override
  public DatumStatusCounter getDatumStatusCounter() {
    DatumStatusCounter counters = new DatumStatusCounter();
    counters.incrementAttempt(this.totalRecordsWritten.get());
    counters.incrementStatus(DatumStatus.SUCCESS, this.totalRecordsWritten.get());
    return counters;
  }
}
