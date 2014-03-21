package org.apache.streams.core;

import java.io.Serializable;

/**
 *
 */
public interface StreamsOperation extends Serializable {

    /**
     * This method will be called after initialization/serialization. Initialize any non-serializable objects here.
     * @param configurationObject Any object to help intialize the operation. ie. Map, JobContext, Properties, etc. The type
     *                            will be based on where the operation is being run (ie. hadoop, storm, locally, etc.)
     */
    public void prepare(Object configurationObject);

    /**
     * No guarantee that this method will ever be called.  But upon shutdown of the stream, an attempt to call this method
     * will be made.
     * Use this method to terminate connections, etc.
     */
    public void cleanUp();
}
