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

package org.apache.streams.hdfs;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.LineReadWriteUtil;
import org.apache.streams.core.DatumStatusCountable;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Queues;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * WebHdfsPersistReader reads from hdfs.
 */
public class WebHdfsPersistReader implements StreamsPersistReader, DatumStatusCountable {

  public static final String STREAMS_ID = "WebHdfsPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistReader.class);

  protected static final char DELIMITER = '\t';

  protected FileSystem client;
  protected Path path;
  protected FileStatus[] status;

  protected volatile Queue<StreamsDatum> persistQueue;

  protected ObjectMapper mapper;
  protected LineReadWriteUtil lineReaderUtil;

  protected HdfsReaderConfiguration hdfsConfiguration;
  protected StreamsConfiguration streamsConfiguration;

  private ExecutorService executor;

  protected DatumStatusCounter countersTotal = new DatumStatusCounter();
  protected DatumStatusCounter countersCurrent = new DatumStatusCounter();
  private Future<?> task;

  /**
   * WebHdfsPersistReader constructor - resolves HdfsReaderConfiguration from JVM 'hdfs'.
   */
  public WebHdfsPersistReader() {
    this(new ComponentConfigurator<>(HdfsReaderConfiguration.class).detectConfiguration());
  }

  /**
   * WebHdfsPersistReader constructor - uses supplied HdfsReaderConfiguration.
   * @param hdfsConfiguration hdfsConfiguration
   */
  public WebHdfsPersistReader(HdfsReaderConfiguration hdfsConfiguration) {
    this.hdfsConfiguration = hdfsConfiguration;
  }

  /**
   * getURI from hdfsConfiguration.
   * @return URI
   * @throws URISyntaxException URISyntaxException
   */
  public URI getURI() throws URISyntaxException {
    StringBuilder uriBuilder = new StringBuilder();
    uriBuilder.append(hdfsConfiguration.getScheme());
    uriBuilder.append("://");
    if (StringUtils.isNotBlank(hdfsConfiguration.getHost())) {
      uriBuilder.append(hdfsConfiguration.getHost());
      if (hdfsConfiguration.getPort() != null) {
        uriBuilder.append(":" + hdfsConfiguration.getPort());
      }
    } else {
      uriBuilder.append("/");
    }
    return new URI(uriBuilder.toString());
  }

  /**
   * isConnected.
   * @return true if connected, false otherwise
   */
  public boolean isConnected() {
    return (client != null);
  }

  /**
   * getFileSystem.
   * @return FileSystem
   */
  public final synchronized FileSystem getFileSystem() {
    // Check to see if we are connected.
    if (!isConnected()) {
      connectToWebHDFS();
    }
    return this.client;
  }

  // TODO: combine with WebHdfsPersistReader.connectToWebHDFS
  private synchronized void connectToWebHDFS() {
    try {
      LOGGER.info("User : {}", this.hdfsConfiguration.getUser());
      UserGroupInformation ugi = UserGroupInformation.createRemoteUser(this.hdfsConfiguration.getUser());
      ugi.setAuthenticationMethod(UserGroupInformation.AuthenticationMethod.SIMPLE);

      ugi.doAs((PrivilegedExceptionAction<Void>) () -> {
        Configuration conf = new Configuration();
        //conf.set(CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
        conf.set("fs.hdfs.impl", DistributedFileSystem.class.getName());
        conf.set("fs.file.impl", LocalFileSystem.class.getName());
        LOGGER.info("WebURI : {}", getURI().toString());
        client = FileSystem.get(getURI(), conf);
        LOGGER.info("Connected to WebHDFS");

        /*
        * ************************************************************************************************
        * This code is an example of how you would work with HDFS and you weren't going over
        * the webHDFS protocol.
        *
        * Smashew: 2013-10-01
        * ************************************************************************************************
        conf.set("fs.defaultFS", "hdfs://hadoop.mdigitallife.com:8020/user/" + userName);
        conf.set("namenode.host","0.0.0.0");
        conf.set("hadoop.job.ugi", userName);
        conf.set(DFSConfigKeys.DFS_NAMENODE_USER_NAME_KEY, "runner");
        fileSystem.createNewFile(new Path("/user/"+ userName + "/test"));
        FileStatus[] status = fs.listStatus(new Path("/user/" + userName));
        for(int i=0;i<status.length;i++)
        {
            LOGGER.info("Directory: {}", status[i].getPath());
        }
        */
        return null;
      });
    } catch (Exception ex) {
      LOGGER.error("There was an error connecting to WebHDFS, please check your settings and try again");
      ex.printStackTrace();
    }
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void prepare(Object configurationObject) {
    LOGGER.debug("Prepare");
    lineReaderUtil = LineReadWriteUtil.getInstance(hdfsConfiguration);
    connectToWebHDFS();
    String pathString = hdfsConfiguration.getPath() + "/" + hdfsConfiguration.getReaderPath();
    LOGGER.info("Path : {}", pathString);
    path = new Path(pathString);
    try {
      if ( client.isFile(path)) {
        LOGGER.info("Found File");
        FileStatus fileStatus = client.getFileStatus(path);
        status = new FileStatus[1];
        status[0] = fileStatus;
      } else if ( client.isDirectory(path)) {
        status = client.listStatus(path);
        List<FileStatus> statusList = Arrays.asList(status);
        Collections.sort(statusList);
        status = statusList.toArray(new FileStatus[0]);
        LOGGER.info("Found Directory : {} files", status.length);
      } else {
        LOGGER.error("Neither file nor directory, wtf");
      }
    } catch (IOException ex) {
      LOGGER.error("IOException", ex);
    }
    streamsConfiguration = StreamsConfigurator.detectConfiguration();
    persistQueue = Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(streamsConfiguration.getBatchSize().intValue()));
    executor = Executors.newSingleThreadExecutor();
    mapper = StreamsJacksonMapper.getInstance();
  }

  @Override
  public void cleanUp() {

  }

  @Override
  public StreamsResultSet readAll() {
    WebHdfsPersistReaderTask readerTask = new WebHdfsPersistReaderTask(this);
    Thread readerThread = new Thread(readerTask);
    readerThread.start();
    try {
      readerThread.join();
    } catch (InterruptedException ignored) {
      LOGGER.trace("ignored InterruptedException", ignored);
    }
    return new StreamsResultSet(persistQueue);
  }

  @Override
  public void startStream() {
    LOGGER.debug("startStream");
    task = executor.submit(new WebHdfsPersistReaderTask(this));
  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet current;

    synchronized ( WebHdfsPersistReader.class ) {
      current = new StreamsResultSet(new ConcurrentLinkedQueue<>(persistQueue));
      current.setCounter(new DatumStatusCounter());
      current.getCounter().add(countersCurrent);
      countersTotal.add(countersCurrent);
      countersCurrent = new DatumStatusCounter();
      persistQueue.clear();
    }

    return current;
  }

  protected void write( StreamsDatum entry ) {
    boolean success;
    do {
      synchronized ( WebHdfsPersistReader.class ) {
        success = persistQueue.offer(entry);
      }
      Thread.yield();
    }
    while ( !success );
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    return null;
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    return null;
  }

  @Override
  public boolean isRunning() {
    return task == null || !task.isDone() && !task.isCancelled();
  }

  @Override
  public DatumStatusCounter getDatumStatusCounter() {
    return countersTotal;
  }
}
