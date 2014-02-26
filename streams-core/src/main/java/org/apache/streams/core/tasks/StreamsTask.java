package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;

import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Interface for all task that will be used to execute instances of {@link org.apache.streams.core.StreamsOperation}
 * in local mode.
 */
public interface StreamsTask extends Runnable{

    public static final long DEFAULT_SLEEP_TIME_MS = 500;

    /**
     * Informs the task to stop. Tasks may or may not try to empty its inbound queue before halting.
     */
    public void stopTask();

    /**
     * Add an input {@link java.util.Queue} for this task.
     * @param inputQueue
     */
    public void addInputQueue(Queue<StreamsDatum> inputQueue);

    /**
     * Add an output {@link java.util.Queue} for this task.
     * @param outputQueue
     */
    public void addOutputQueue(Queue<StreamsDatum> outputQueue);

    /**
     * Set the configuration object that will shared and passed to all instances of StreamsTask.
     * @param config optional configuration information
     */
    public void setStreamConfig(Map<String, Object> config);

    /**
     * Returns true when the task has not completed. Returns false otherwise
     * @return true when the task has not completed. Returns false otherwise
     */
    public boolean isRunning();

    /**
     * Returns the input queues that have been set for this task.
     * @return list of input queues
     */
    public List<Queue<StreamsDatum>> getInputQueues();

    /**
     * Returns the output queues that have been set for this task
     * @return list of output queues
     */
    public List<Queue<StreamsDatum>> getOutputQueues();

}
