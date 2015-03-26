package org.apache.streams.s3;

public class SimpleS3OutputStreamCallback implements S3OutputStreamWrapperCloseCallback {

    private boolean closed = false;
    private boolean error = false;

    public boolean isClosed()   { return this.closed; }
    public boolean isError()    { return this.error; }

    @Override
    public void completed() {
        this.closed = true;
    }

    @Override
    public void error() {
        this.closed = true;
        this.error = true;
    }

}
