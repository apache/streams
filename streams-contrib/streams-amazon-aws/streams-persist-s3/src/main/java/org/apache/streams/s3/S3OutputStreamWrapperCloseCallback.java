package org.apache.streams.s3;

public interface S3OutputStreamWrapperCloseCallback {
    void completed();
    void error();
}
