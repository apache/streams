package org.apache.streams.local.monitors;

import org.apache.streams.core.DatumStatusCountable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TimerTask;

public class StatusCounterMonitorThread extends TimerTask
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCounterMonitorThread.class);

    private final DatumStatusCountable task;

    public StatusCounterMonitorThread(DatumStatusCountable task) {
        this.task = task;
    }

    public void run() {
        LOGGER.info("{}: {} attempted, {} success, {} partial, {} failed, {} total",
                task.getClass(),
                task.getDatumStatusCounter().getAttempted(),
                task.getDatumStatusCounter().getSuccess(),
                task.getDatumStatusCounter().getPartial(),
                task.getDatumStatusCounter().getFail(),
                task.getDatumStatusCounter().getEmitted());
    }
}
