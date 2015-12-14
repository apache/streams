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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.LineReadWriteUtil;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCountable;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

public class WebHdfsPersistWriter implements StreamsPersistWriter, Flushable, Closeable, DatumStatusCountable {

    public final static String STREAMS_ID = "WebHdfsPersistWriter";

    private final static Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistWriter.class);

    private FileSystem client;
    private Path path;
    private int linesPerFile;
    private int totalRecordsWritten = 0;
    private final List<Path> writtenFiles = new ArrayList<Path>();
    private int fileLineCounter = 0;
    private OutputStreamWriter currentWriter = null;

    private static final int BYTES_IN_MB = 1024 * 1024;
    private static final int BYTES_BEFORE_FLUSH = 64 * BYTES_IN_MB;
    private volatile int totalByteCount = 0;
    private volatile int byteCount = 0;

    public boolean terminate = false;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper;
    private LineReadWriteUtil lineWriterUtil;

    protected HdfsWriterConfiguration hdfsConfiguration;

    public WebHdfsPersistWriter() {
        this(new ComponentConfigurator<>(HdfsWriterConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("hdfs")));
    }

    public WebHdfsPersistWriter(HdfsWriterConfiguration hdfsConfiguration) {
        this.hdfsConfiguration = hdfsConfiguration;
        this.linesPerFile = hdfsConfiguration.getLinesPerFile().intValue();
    }

    public URI getURI() throws URISyntaxException {
        StringBuilder uriBuilder = new StringBuilder();
        uriBuilder.append(hdfsConfiguration.getScheme());
        uriBuilder.append("://");
        if( !Strings.isNullOrEmpty(hdfsConfiguration.getHost()))
            uriBuilder.append(hdfsConfiguration.getHost() + ":" + hdfsConfiguration.getPort());
        else
            uriBuilder.append("/");
        return new URI(uriBuilder.toString());
    }

    public boolean isConnected() {
        return (client != null);
    }

    public final synchronized FileSystem getFileSystem() {
        // Check to see if we are connected.
        if (!isConnected())
            connectToWebHDFS();
        return this.client;
    }

    private synchronized void connectToWebHDFS() {
        try {
            LOGGER.info("User : {}", this.hdfsConfiguration.getUser());
            UserGroupInformation ugi = UserGroupInformation.createRemoteUser(this.hdfsConfiguration.getUser());
            ugi.setAuthenticationMethod(UserGroupInformation.AuthenticationMethod.SIMPLE);

            ugi.doAs(new PrivilegedExceptionAction<Void>() {
                public Void run() throws Exception {
                    Configuration conf = new Configuration();
                    conf.set(CommonConfigurationKeysPublic.HADOOP_SECURITY_AUTHENTICATION, "kerberos");
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
                }
            });
        } catch (Exception e) {
            LOGGER.error("There was an error connecting to WebHDFS, please check your settings and try again", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        synchronized (this) {
            // Check to see if we need to reset the file that we are currently working with
            if (this.currentWriter == null || (this.fileLineCounter > this.linesPerFile))
                resetFile();

            String line = lineWriterUtil.convertResultToString(streamsDatum);
            writeInternal(line);
            if( !line.endsWith(this.hdfsConfiguration.getLineDelimiter()))
                writeInternal(this.hdfsConfiguration.getLineDelimiter());
            int bytesInLine = line.getBytes().length;

            totalRecordsWritten++;
            totalByteCount += bytesInLine;
            byteCount += bytesInLine;

            if (byteCount > BYTES_BEFORE_FLUSH)
                try {
                    flush();
                } catch (IOException e) {
                    LOGGER.warn("Error flushing to HDFS. Creating a new file and continuing execution.  WARNING: There could be data loss.", e);
                }

            this.fileLineCounter++;
        }
    }

    private void writeInternal(String line) {
        try {
            this.currentWriter.write(line);
        } catch (IOException e) {
            LOGGER.warn("Error writing to HDFS.  Attempting to try a new file", e);
            try{
                resetFile();
                this.currentWriter.write(line);
            } catch (Exception io) {
                LOGGER.warn("Failed to write even after creating a new file.  Attempting to reconnect", io);
                try {
                    connectToWebHDFS();
                    resetFile();
                    this.currentWriter.write(line);
                } catch (Exception ex) {
                    LOGGER.error("Failed to write to HDFS after reconnecting client. Terminating writer.", ex);
                    throw new RuntimeException(e);
                }
            }

        }
    }

    public void flush() throws IOException {
        if (this.currentWriter != null && byteCount > BYTES_BEFORE_FLUSH) {
            this.currentWriter.flush();
            byteCount = 0;
        }
    }

    private synchronized void resetFile() {
        // this will keep it thread safe, so we don't create too many files
        if (this.fileLineCounter == 0 && this.currentWriter != null)
            return;

        // Create the path for where the file is going to live.
        Path filePath = this.path.suffix("/" + hdfsConfiguration.getWriterFilePrefix() + "-" + new Date().getTime());

        if( hdfsConfiguration.getCompression().equals(HdfsWriterConfiguration.Compression.GZIP))
            filePath = filePath.suffix(".gz");
        else
            filePath = filePath.suffix(".tsv");

        try {

            // if there is a current writer, we must close it first.
            if (this.currentWriter != null) {
                flush();
                close();
            }

            this.fileLineCounter = 0;

            // Check to see if a file of the same name exists, if it does, then we are not going to be able to proceed.
            if (client.exists(filePath))
                throw new RuntimeException("Unable to create file: " + filePath);

            if( hdfsConfiguration.getCompression().equals(HdfsWriterConfiguration.Compression.GZIP))
                this.currentWriter = new OutputStreamWriter(new GZIPOutputStream(client.create(filePath)));
            else
                this.currentWriter = new OutputStreamWriter(client.create(filePath));

            // Add another file to the list of written files.
            writtenFiles.add(filePath);

            LOGGER.info("File Created: {}", filePath);
        } catch (Exception e) {
            LOGGER.error("COULD NOT CreateFile: {}", filePath);
            LOGGER.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized void close() throws IOException {
        if (this.currentWriter != null) {
            this.currentWriter.flush();
            this.currentWriter.close();
            this.currentWriter = null;
            LOGGER.info("File Closed");
        }
    }

    @Override
    public void prepare(Object configurationObject) {
        mapper = StreamsJacksonMapper.getInstance();
        lineWriterUtil = LineReadWriteUtil.getInstance(hdfsConfiguration);
        connectToWebHDFS();
        path = new Path(hdfsConfiguration.getPath() + "/" + hdfsConfiguration.getWriterPath());
    }

    @Override
    public void cleanUp() {
        try {
            flush();
        } catch (IOException e) {
            LOGGER.error("Error flushing on cleanup", e);
        }
        try {
            close();
        } catch (IOException e) {
            LOGGER.error("Error closing on cleanup", e);
        }
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        DatumStatusCounter counters = new DatumStatusCounter();
        counters.incrementAttempt(this.totalRecordsWritten);
        counters.incrementStatus(DatumStatus.SUCCESS, this.totalRecordsWritten);
        return counters;
    }
}
