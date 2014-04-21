package org.apache.streams.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.Map;

/**
 *
 * Author:  Smashew
 * Date:    2014-04-14
 *
 * Description:
 * This class uses ByteArrayOutputStreams to ensure files are written to S3 properly.
 *
 * There is a way to upload data in chunks (5mb or so) a peice, but the multi-part upload
 * is kind of a PITA to deal with.
 *
 * // TODO: This should be refactored to allow a user to specify if they want one large file instead of many small ones
 */
public class S3OutputStreamWrapper extends OutputStream
{
    private static final Logger LOGGER = LoggerFactory.getLogger(S3OutputStreamWrapper.class);

    private final AmazonS3Client amazonS3Client;
    private final String bucketName;
    private final String path;
    private final String fileName;
    private final ByteArrayOutputStream outputStream;
    private final Map<String, String> metaData;

    private String errorMessage;
    private boolean isClosed = false;

    public boolean isClosed()                                           { return this.isClosed; }
    public String getErrorMessage()                                     { return this.errorMessage; }
    public boolean hasError()                                           { return this.errorMessage != null; }


    public S3OutputStreamWrapper(AmazonS3Client amazonS3Client, String bucketName, String path, String fileName, Map<String, String> metaData) throws IOException
    {
        this.amazonS3Client = amazonS3Client;
        this.bucketName = bucketName;
        this.path = path;
        this.fileName = fileName;
        this.metaData = metaData;
        this.outputStream = new ByteArrayOutputStream();
    }

    /*
     * The Methods that are overriden to support the 'OutputStream' object.
     */

    public void write(int b) throws IOException                         { this.outputStream.write(b); }
    public void write(byte[] b) throws IOException                      { this.outputStream.write(b); }
    public void write(byte[] b, int off, int len) throws IOException    { this.outputStream.write(b, off, len); }
    public void flush() throws IOException                              { this.outputStream.flush(); }

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
            }
            catch(Exception e) {
                e.printStackTrace();
                LOGGER.warn("There was an error adding the temporaryFile to S3");
                this.errorMessage = "Error: Exception - " + e.getMessage();
            }
            finally {
                // we are done here.
                this.isClosed = true;
            }
        }
    }

    private void addFile() throws Exception {

        TransferManager transferManager = new TransferManager(this.amazonS3Client);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setExpirationTime(DateTime.now().plusDays(365*3).toDate());
        metadata.setContentLength(this.outputStream.size());

        metadata.addUserMetadata("writer", "org.apache.streams");
        for(String s : this.metaData.keySet()) {
            metadata.addUserMetadata(s, this.metaData.get(s));
        }

        InputStream is = new ByteArrayInputStream(this.outputStream.toByteArray());
        String fileNameToWrite = this.path + fileName;
        Upload upload = transferManager.upload(this.bucketName, fileNameToWrite, is, metadata);
        upload.waitForUploadResult();

        is.close();

        LOGGER.info("File Written to S3: {}", metadata);
    }


}
