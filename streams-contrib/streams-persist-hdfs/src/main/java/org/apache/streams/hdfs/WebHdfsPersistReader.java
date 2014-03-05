package org.apache.streams.hdfs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.hdfs.web.WebHdfsFileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 2/28/14.
 */
public class WebHdfsPersistReader implements StreamsPersistReader {

    public final static String STREAMS_ID = "WebHdfsPersistReader";

    private final static Logger LOGGER = LoggerFactory.getLogger(WebHdfsPersistReader.class);

    protected final static char DELIMITER = '\t';

    protected FileSystem client;
    protected Path path;
    protected FileStatus[] status;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private HdfsReaderConfiguration hdfsConfiguration;

    private ExecutorService executor;

    public WebHdfsPersistReader(HdfsReaderConfiguration hdfsConfiguration) {
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
    public void prepare(Object configurationObject) {
        LOGGER.debug("Prepare");
        connectToWebHDFS();
        path = new Path(hdfsConfiguration.getPath() + "/" + hdfsConfiguration.getReaderPath());
        try {
            status = client.listStatus(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void cleanUp() {

    }

    @Override
    public StreamsResultSet readAll() {
        readSourceWritePersistQueue();
        return new StreamsResultSet(persistQueue);
    }

    @Override
    public void startStream() {
        LOGGER.debug("startStream");
        executor.submit(new WebHdfsPersistReaderTask(this));
    }

    @Override
    public StreamsResultSet readCurrent() {

        LOGGER.debug("readCurrent: {}", persistQueue.size());

        Collection<StreamsDatum> currentIterator = Lists.newArrayList();
        Iterators.addAll(currentIterator, persistQueue.iterator());

        StreamsResultSet current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(currentIterator));

        persistQueue.clear();

        return current;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    private void readSourceWritePersistQueue() {
        for( FileStatus fileStatus : status ) {
            BufferedReader reader;

            if( fileStatus.isFile() && !fileStatus.getPath().getName().endsWith("_SUCCESS")) {
                try {
                    reader = new BufferedReader(new InputStreamReader(client.open(fileStatus.getPath())));

                    String line;
                    do{
                        try {
                            line = reader.readLine();
                            if( line != null ) {
                                String[] fields = line.split(Character.toString(DELIMITER));
                                persistQueue.offer(new StreamsDatum(fields[3]));
                            }
                        } catch (IOException e) {
                            break;
                        }
                    } while( line != null );
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
