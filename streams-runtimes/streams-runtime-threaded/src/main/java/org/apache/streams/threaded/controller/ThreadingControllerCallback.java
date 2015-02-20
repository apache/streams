package org.apache.streams.threaded.controller;

public abstract class ThreadingControllerCallback {
    public abstract void onSuccess(Object o);
    public abstract void onFailure(Throwable t);
}
