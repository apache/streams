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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Queues;
import org.apache.streams.core.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

public class S3PersistReader implements StreamsPersistReader, DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(S3PersistReader.class);
    public final static String STREAMS_ID = "S3PersistReader";
    protected final static char DELIMITER = '\t';

    private S3ReaderConfiguration s3ReaderConfiguration;
    private AmazonS3Client amazonS3Client;
    private ObjectMapper mapper = new ObjectMapper();
    private Collection<String> files;
    private ExecutorService executor;
    protected volatile Queue<StreamsDatum> persistQueue;

    protected DatumStatusCounter countersTotal = new DatumStatusCounter();
    protected DatumStatusCounter countersCurrent = new DatumStatusCounter();
    private Future<?> task;

    public AmazonS3Client getAmazonS3Client() {
        return this.amazonS3Client;
    }

    public S3ReaderConfiguration getS3ReaderConfiguration() {
        return this.s3ReaderConfiguration;
    }

    public String getBucketName() {
        return this.s3ReaderConfiguration.getBucket();
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return !task.isDone() && !task.isCancelled();
    }

    public DatumStatusCounter getDatumStatusCounter() {
        return countersTotal;
    }

    public Collection<String> getFiles() {
        return this.files;
    }

    public S3PersistReader(S3ReaderConfiguration s3ReaderConfiguration) {
        this.s3ReaderConfiguration = s3ReaderConfiguration;
    }

    public void prepare(Object configurationObject) {
        // Connect to S3
        synchronized (this)
        {
            // Create the credentials Object
            AWSCredentials credentials = new BasicAWSCredentials(s3ReaderConfiguration.getKey(), s3ReaderConfiguration.getSecretKey());

            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.valueOf(s3ReaderConfiguration.getProtocol().toString()));

            // We do not want path style access
            S3ClientOptions clientOptions = new S3ClientOptions();
            clientOptions.setPathStyleAccess(false);

            this.amazonS3Client = new AmazonS3Client(credentials, clientConfig);
            if( !Strings.isNullOrEmpty(s3ReaderConfiguration.getRegion()))
                this.amazonS3Client.setRegion(Region.getRegion(Regions.fromName(s3ReaderConfiguration.getRegion())));
            this.amazonS3Client.setS3ClientOptions(clientOptions);
        }

        final ListObjectsRequest request = new ListObjectsRequest()
                .withBucketName(this.s3ReaderConfiguration.getBucket())
                .withPrefix(s3ReaderConfiguration.getReaderPath())
                .withMaxKeys(500);


        ObjectListing listing = this.amazonS3Client.listObjects(request);

        this.files = new ArrayList<String>();

        /**
         * If you can list files that are in this path, then you must be dealing with a directory
         * if you cannot list files that are in this path, then you are most likely dealing with
         * a simple file.
         */
        boolean hasCommonPrefixes = listing.getCommonPrefixes().size() > 0 ? true : false;
        boolean hasObjectSummaries = listing.getObjectSummaries().size() > 0 ? true : false;

        if(hasCommonPrefixes || hasObjectSummaries) {
            // Handle the 'directory' use case
            do
            {
                if(hasCommonPrefixes) {
                    for (String file : listing.getCommonPrefixes()) {
                        this.files.add(file);
                    }
                } else {
                    for(final S3ObjectSummary objectSummary : listing.getObjectSummaries()) {
                        this.files.add(objectSummary.getKey());
                    }
                }

                // get the next batch.
                listing = this.amazonS3Client.listNextBatchOfObjects(listing);
            } while (listing.isTruncated());
        }
        else {
            // handle the single file use-case
            this.files.add(s3ReaderConfiguration.getReaderPath());
        }

        if(this.files.size() <= 0)
            LOGGER.error("There are no files to read");

        this.persistQueue = Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(10000));
        this.executor = Executors.newSingleThreadExecutor();
    }

    public void cleanUp() {
        // no Op
    }

    public StreamsResultSet readAll() {
        startStream();
        return new StreamsResultSet(persistQueue);
    }

    public void startStream() {
        LOGGER.debug("startStream");
        task = executor.submit(new S3PersistReaderTask(this));
    }

    public StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized( S3PersistReader.class ) {
            current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(persistQueue));
            current.setCounter(new DatumStatusCounter());
            current.getCounter().add(countersCurrent);
            countersTotal.add(countersCurrent);
            countersCurrent = new DatumStatusCounter();
            persistQueue.clear();
        }
        return current;
    }

}
