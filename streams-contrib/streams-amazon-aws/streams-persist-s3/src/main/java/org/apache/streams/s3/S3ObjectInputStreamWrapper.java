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

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * There is a nuance associated with reading portions of files in S3. Everything occurs over
 * an Apache HTTP client object. Apache and therefore Amazon defaults to re-using the stream.
 * As a result, if you only intend read a small portion of the file. You must first "abort" the
 * stream, then close the 'inputStream'. Otherwise, Apache will exhaust the entire stream
 * and transfer the entire file. If you are only reading the first 50 lines of a 5,000,000 line file
 * this becomes problematic.
 *
 * This class operates as a wrapper to fix the aforementioned nuances.
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

    /**
     * Create an input stream safely from
     * @param s3Object
     */
    public S3ObjectInputStreamWrapper(S3Object s3Object) {
        this.s3Object = s3Object;
        this.is = this.s3Object.getObjectContent();
    }

    public int hashCode() {
        return this.is.hashCode();
    }

    public boolean equals(Object obj) {
        return this.is.equals(obj);
    }

    public String toString() {
        return this.is.toString();
    }

    public int read() throws IOException {
        return this.is.read();
    }

    public int read(byte[] b) throws IOException {
        return this.is.read(b);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return this.is.read(b, off, len);
    }

    public long skip(long n) throws IOException {
        return this.is.skip(n);
    }

    public int available() throws IOException {
        return this.is.available();
    }

    public boolean markSupported() {
        return this.is.markSupported();
    }

    public synchronized void mark(int readlimit) {
        this.is.mark(readlimit);
    }

    public synchronized void reset() throws IOException {
        this.is.reset();
    }

    public void close() throws IOException {
        ensureEverythingIsReleased();
    }

    public void ensureEverythingIsReleased() {
        if(this.isClosed)
            return;


        try {
            // ensure that the S3 Object is closed properly.
            this.s3Object.close();
        } catch(Throwable e) {
            LOGGER.warn("Problem Closing the S3Object[{}]: {}", s3Object.getKey(), e.getMessage());
        }


        try {
            // Abort the stream
            this.is.abort();
        }
        catch(Throwable e) {
            LOGGER.warn("Problem Aborting S3Object[{}]: {}", s3Object.getKey(), e.getMessage());
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
        try {
            // If there is an accidental leak where the user did not close, call this on the classes destructor
            ensureEverythingIsReleased();
            super.finalize();
        } catch(Exception e) {
            // this should never be called, just being very cautious
            LOGGER.warn("S3InputStreamWrapper: Issue Releasing Connections on Finalize - {}", e.getMessage());
        }
    }

}
