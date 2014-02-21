package org.apache.streams.core;

/**
 * Created by sblackmon on 1/6/14.
 */
public enum StreamState {
    RUNNING,  //Stream is currently connected and running
    STOPPED,  // Stream has been shut down and is stopped
    CONNECTING, //Stream is attempting to connect to server
    SHUTTING_DOWN, //Stream has initialized shutdown
    DISCONNECTED //Stream has unintentionally lost connection
}

