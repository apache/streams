package org.apache.streams.hdfs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.CommonConfigurationKeysPublic;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.streams.hdfs.HdfsConfiguration;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebHdfsPersistWriter implements StreamsPersistWriter, Flushable, Closeable
{
    public final static String STREAMS_ID = "WebHdfsPersistWriter";

    private final static Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistWriter.class);

    private final static char DELIMITER = '\t';
    private final static int  DEFAULT_LINES_PER_FILE = 50000;

    private FileSystem client;
    private Path path;
    private String filePart = "default";
    private int linesPerFile = 1000;
    private int totalRecordsWritten = 0;
    private final List<Path> writtenFiles = new ArrayList<Path>();
    private int fileLineCounter = 0;
    private OutputStreamWriter currentWriter = null;

    private static final int  BYTES_IN_MB = 1024*1024;
    private static final int  BYTES_BEFORE_FLUSH = 5 * BYTES_IN_MB;
    private volatile int  totalByteCount = 0;
    private volatile int  byteCount = 0;

    public boolean terminate = false;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private HdfsWriterConfiguration hdfsConfiguration;

    public WebHdfsPersistWriter(HdfsWriterConfiguration hdfsConfiguration) {
        this.hdfsConfiguration = hdfsConfiguration;
    }

    public URI getURI() throws URISyntaxException { return new URI(WebHdfsFileSystem.SCHEME + "://" + hdfsConfiguration.getHost() + ":" + hdfsConfiguration.getPort()); }
    public boolean isConnected() 		                { return (client != null); }

    public final synchronized FileSystem getFileSystem()
    {
        // Check to see if we are connected.
        if(!isConnected())
            connectToWebHDFS();
        return this.client;
    }

    private synchronized void connectToWebHDFS()
    {
        try
        {
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
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error connecting to WebHDFS, please check your settings and try again");
            e.printStackTrace();
        }
    }
    
    @Override
    public void write(StreamsDatum streamsDatum) {

        synchronized (this)
        {
            // Check to see if we need to reset the file that we are currently working with
            if (this.currentWriter == null || (this.fileLineCounter > this.linesPerFile))
                try {
                    resetFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            String line = convertResultToString(streamsDatum);
            try {
                this.currentWriter.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int bytesInLine = line.getBytes().length;

            totalRecordsWritten++;
            totalByteCount += bytesInLine;
            byteCount += bytesInLine;

            if(byteCount > BYTES_BEFORE_FLUSH)
                try {
                    flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            this.fileLineCounter++;
        }
    }

    public void flush() throws IOException
    {
        if(this.currentWriter != null && byteCount > BYTES_BEFORE_FLUSH)
        {
            this.currentWriter.flush();
            byteCount = 0;
        }
    }

    private synchronized void resetFile() throws Exception
    {
        // this will keep it thread safe, so we don't create too many files
        if(this.fileLineCounter == 0 && this.currentWriter != null)
            return;

        // if there is a current writer, we must close it first.
        if (this.currentWriter != null)
        {
            flush();
            close();
        }

        this.fileLineCounter = 0;

        // Create the path for where the file is going to live.
        Path filePath = this.path.suffix("/" + hdfsConfiguration.getWriterFilePrefix() + "-" + new Date().getTime() + ".tsv");

        try
        {
            // Check to see if a file of the same name exists, if it does, then we are not going to be able to proceed.
            if(client.exists(filePath))
                throw new RuntimeException("Unable to create file: " + filePath);

            this.currentWriter = new OutputStreamWriter(client.create(filePath));

            // Add another file to the list of written files.
            writtenFiles.add(filePath);

            LOGGER.info("File Created: {}", filePath);
        }
        catch (Exception e)
        {
            LOGGER.error("COULD NOT CreateFile: {}", filePath);
            LOGGER.error(e.getMessage());
            throw e;
        }
    }

    public synchronized void close() throws IOException
    {
        if(this.currentWriter != null)
        {
            this.currentWriter.flush();
            this.currentWriter.close();
            this.currentWriter = null;
            LOGGER.info("File Closed");
        }
    }

    private String convertResultToString(StreamsDatum entry)
    {
        String metadata = null;
        try {
            metadata = mapper.writeValueAsString(entry.getMetadata());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String documentJson = null;
        try {
            documentJson = mapper.writeValueAsString(entry.getDocument());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        if(Strings.isNullOrEmpty(documentJson))
            return null;
        else
            return new StringBuilder()
                    .append(entry.getSequenceid())
                    .append(DELIMITER)
                    .append(entry.getTimestamp())
                    .append(DELIMITER)
                    .append(metadata)
                    .append(DELIMITER)
                    .append(documentJson)
                    .append("\n")
                    .toString();
    }

    @Override
    public void prepare(Object configurationObject) {
        connectToWebHDFS();
        path = new Path(hdfsConfiguration.getPath() + "/" + hdfsConfiguration.getWriterPath());
    }

    @Override
    public void cleanUp() {
        try {
            flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
