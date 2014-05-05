package org.apache.streams.s3;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * There is a stupid nuance associated with reading portions of files in S3. Everything occurs over
 * an Apache HTTP client object. Apache defaults to re-using the stream. So, if you only want to read
 * a small portion of the file. You must first "abort" the stream, then close. Otherwise, Apache will
 * exhaust the stream and transfer a ton of data attempting to do so.
 *
 *
 * Author   Smashew
 * Date     2014-04-11
 *
 * After a few more days and some demos that had some issues with concurrency and high user load. This
 * was also discovered. There is an issue with the S3Object's HTTP connection not being released back
 * to the connection pool (until it times out) even once the item is garbage collected. So....
 *
 * Reference:
 * http://stackoverflow.com/questions/17782937/connectionpooltimeoutexception-when-iterating-objects-in-s3
 */
public class S3ObjectInputStreamWrapper extends InputStream
{
    private final static Logger LOGGER = LoggerFactory.getLogger(S3ObjectInputStreamWrapper.class);

    private final S3Object s3Object;
    private final S3ObjectInputStream is;
    private boolean isClosed = false;

    public S3ObjectInputStreamWrapper(S3Object s3Object) {
        this.s3Object = s3Object;
        this.is = this.s3Object.getObjectContent();
    }

    public int hashCode()                                           { return this.is.hashCode(); }
    public boolean equals(Object obj)                               { return this.is.equals(obj); }
    public String toString()                                        { return this.is.toString(); }
    public int read() throws IOException                            { return this.is.read(); }
    public int read(byte[] b) throws IOException                    { return this.is.read(b); }
    public int read(byte[] b, int off, int len) throws IOException  { return this.is.read(b, off, len); }
    public long skip(long n) throws IOException                     { return this.is.skip(n); }
    public int available() throws IOException                       { return this.is.available(); }
    public boolean markSupported()                                  { return this.is.markSupported(); }
    public synchronized void mark(int readlimit)                    { this.is.mark(readlimit); }
    public synchronized void reset() throws IOException             { this.is.reset(); }

    public void close() throws IOException {
        ensureEverythingIsReleased();
    }

    public void ensureEverythingIsReleased()
    {
        if(this.isClosed)
            return;

        // THIS WHOLE CLASS IS JUST FOR THIS FUNCTION!
        // Amazon S3 - HTTP Exhaust all file contents issue
        try {
            this.is.abort();
        }
        catch(Exception e) {
            LOGGER.warn("S3Object[{}]: Issue Aborting Stream - {}", s3Object.getKey(), e.getMessage());
        }

        // close the input Stream Safely
        closeSafely(this.is);

        // This corrects the issue with Open HTTP connections
        closeSafely(this.s3Object);
        this.isClosed = true;
    }

    private static void closeSafely(Closeable is) {
        try {
            if(is != null)
                is.close();
        } catch(Exception e) {
            e.printStackTrace();
            LOGGER.warn("S3InputStreamWrapper: Issue Closing Closeable - {}", e.getMessage());
        }
    }

    protected void finalize( ) throws Throwable
    {
        try
        {
            ensureEverythingIsReleased();
            super.finalize();
        } catch(Exception e) {
            // this should never be called, just being very cautious
            LOGGER.warn("S3InputStreamWrapper: Issue Releasing Connections on Finalize - {}", e.getMessage());
        }
    }

}
