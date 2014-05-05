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

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Map;

/**
 * This class uses ByteArrayOutputStreams to ensure files are written to S3 properly. The stream is written to the
 * in memory ByteArrayOutPutStream before it is finally written to Amazon S3. The size the file is allowed to become
 * is directly controlled by the S3PersistWriter.
 */
public class S3OutputStreamWrapper extends OutputStream
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3OutputStreamWrapper.class);

    private final AmazonS3Client amazonS3Client;
    private final String bucketName;
    private final String path;
    private final String fileName;
    private ByteArrayOutputStream outputStream;
    private final Map<String, String> metaData;
    private boolean isClosed = false;

    /**
     * Create an OutputStream Wrapper
     * @param amazonS3Client
     * The Amazon S3 Client which will be handling the file
     * @param bucketName
     * The Bucket Name you are wishing to write to.
     * @param path
     * The path where the object will live
     * @param fileName
     * The fileName you ware wishing to write.
     * @param metaData
     * Any meta data that is to be written along with the object
     * @throws IOException
     * If there is an issue creating the stream, this
     */
    public S3OutputStreamWrapper(AmazonS3Client amazonS3Client, String bucketName, String path, String fileName, Map<String, String> metaData) throws IOException {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.path = path;
        this.fileName = fileName;
        this.metaData = metaData;
        this.outputStream = new ByteArrayOutputStream();
    }

    public void write(int b) throws IOException {
        this.outputStream.write(b);
    }

    public void write(byte[] b) throws IOException {
        this.outputStream.write(b);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        this.outputStream.write(b, off, len);
    }

    public void flush() throws IOException {
        this.outputStream.flush();
    }

    /**
     * Whenever the output stream is closed we are going to kick the ByteArrayOutputStream off to Amazon S3.
     * @throws IOException
     * Exception thrown from the FileOutputStream
     */
    public void close() throws IOException {
        if(!isClosed)
        {
            try
            {
                this.addFile();
                this.outputStream.close();
                this.outputStream = null;
            }
            catch(Exception e) {
                e.printStackTrace();
                LOGGER.warn("There was an error adding the temporaryFile to S3");
            }
            finally {
                // we are done here.
                this.isClosed = true;
            }
        }
    }

    private void addFile() throws Exception {

        InputStream is = new ByteArrayInputStream(this.outputStream.toByteArray());
        int contentLength = outputStream.size();

        TransferManager transferManager = new TransferManager(amazonS3Client);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setExpirationTime(DateTime.now().plusDays(365*3).toDate());
        metadata.setContentLength(contentLength);

        metadata.addUserMetadata("writer", "org.apache.streams");

        for(String s : metaData.keySet())
            metadata.addUserMetadata(s, metaData.get(s));

        String fileNameToWrite = path + fileName;
        Upload upload = transferManager.upload(bucketName, fileNameToWrite, is, metadata);
        try {
            upload.waitForUploadResult();

            is.close();
            transferManager.shutdownNow(false);
            LOGGER.info("S3 File Close[{} kb] - {}", contentLength / 1024, path + fileName);
        } catch (Exception e) {
            // No Op
        }


    }


}
