package org.apache.streams.core.util;

import org.apache.streams.core.StreamsDatum;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class QueueUtils {

  public static Queue<StreamsDatum> constructQueue() {
    return new LinkedBlockingQueue<>();
  }

}
