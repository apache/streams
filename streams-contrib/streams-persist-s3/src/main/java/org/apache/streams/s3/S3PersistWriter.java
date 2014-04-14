package org.apache.streams.s3;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.streams.core.*;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class S3PersistWriter implements StreamsPersistWriter, Flushable, Closeable, DatumStatusCountable
{
    public final static String STREAMS_ID = "S3PersistWriter";

    private final static Logger LOGGER = LoggerFactory.getLogger(S3PersistWriter.class);

    private final static char DELIMITER = '\t';

    private final AtomicInteger totalRecordsWritten = new AtomicInteger();
    private AtomicInteger fileLineCounter = new AtomicInteger();

    private AmazonS3Client amazonS3Client;
    private S3WriterConfiguration s3WriterConfiguration;

    private final List<String> writtenFiles = new ArrayList<String>();
    private OutputStreamWriter currentWriter = null;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    public AmazonS3Client getAmazonS3Client()                           { return this.amazonS3Client; }
    public List<String> getWrittenFiles()                               { return this.writtenFiles; }


    public S3PersistWriter(S3WriterConfiguration s3WriterConfiguration) {
        this.s3WriterConfiguration = s3WriterConfiguration;
    }

    private synchronized void connectToWebHDFS()
    {
        // Connect to S3
        synchronized (this)
        {
            // Create the credentials Object
            AWSCredentials credentials = new BasicAWSCredentials(s3WriterConfiguration.getKey(), s3WriterConfiguration.getSecretKey());

            ClientConfiguration clientConfig = new ClientConfiguration();
            clientConfig.setProtocol(Protocol.valueOf(s3WriterConfiguration.getProtocol().toUpperCase()));

            // We want path style access
            S3ClientOptions clientOptions = new S3ClientOptions();
            clientOptions.setPathStyleAccess(true);

            this.amazonS3Client = new AmazonS3Client(credentials, clientConfig);
            this.amazonS3Client.setS3ClientOptions(clientOptions);
        }
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        synchronized (this)
        {
            // Check to see if we need to reset the file that we are currently working with
            if (this.currentWriter == null || (this.fileLineCounter.get() > this.s3WriterConfiguration.getLinesPerFile()))
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

            totalRecordsWritten.incrementAndGet();

            this.fileLineCounter.incrementAndGet();
        }
    }

    public void flush() throws IOException {
        // This is wrapped with a ByteArrayOutputStream, so this is reallly safe.
        this.currentWriter.flush();
    }

    private synchronized void resetFile() throws Exception
    {
        // this will keep it thread safe, so we don't create too many files
        if(this.fileLineCounter.get() == 0 && this.currentWriter != null)
            return;

        // if there is a current writer, we must close it first.
        if (this.currentWriter != null) {
            flush();
            close();
        }

        this.fileLineCounter = new AtomicInteger();

        // Create the path for where the file is going to live.
        try
        {
            String fileName = this.s3WriterConfiguration.getWriterFilePrefix() + "-" + new Date().getTime() + ".tsv";
            this.currentWriter = new OutputStreamWriter(new S3OutputStreamWrapper(this.amazonS3Client, this.s3WriterConfiguration.getBucket(), this.s3WriterConfiguration.getWriterPath(), fileName));

            // Add another file to the list of written files.
            writtenFiles.add(this.s3WriterConfiguration.getWriterPath() + fileName);

            LOGGER.info("File Created: Bucket[{}] - {}", this.s3WriterConfiguration.getBucket(), this.s3WriterConfiguration.getWriterPath() + fileName);
        }
        catch (Exception e)
        {
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

    public void prepare(Object configurationObject) {
        connectToWebHDFS();
    }

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

    public DatumStatusCounter getDatumStatusCounter() {
        DatumStatusCounter counters = new DatumStatusCounter();
        counters.incrementAttempt(this.totalRecordsWritten.get());
        counters.incrementStatus(DatumStatus.SUCCESS, this.totalRecordsWritten.get());
        return counters;
    }
}
