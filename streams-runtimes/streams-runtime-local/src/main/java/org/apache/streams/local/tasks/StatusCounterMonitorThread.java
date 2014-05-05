package org.apache.streams.local.tasks;

import org.apache.streams.core.DatumStatusCountable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCounterMonitorThread implements StatusCounterMonitorRunnable
{
    private static final Logger LOGGER = LoggerFactory.getLogger(StatusCounterMonitorThread.class);

    private DatumStatusCountable task;

    private int seconds;

    private boolean run = true;

    public StatusCounterMonitorThread(DatumStatusCountable task, int delayInSeconds) {
        this.task = task;
        this.seconds = delayInSeconds;
    }

    @Override
    public void shutdown(){
        this.run = false;
    }

    @Override
    public boolean isRunning() {
        return this.run;
    }

    @Override
    public void run()
    {
        while(run){

            /**
             *
             * Note:
             * Quick class and method to let us see what is going on with the JVM. We need to make sure
             * that everything is running with as little memory as possible. If we are generating a heap
             * overflow, this will be very apparent by the information shown here.
             */

            LOGGER.debug("{}: {} attempted, {} success, {} partial, {} failed, {} total",
                    task.getClass(),
                    task.getDatumStatusCounter().getAttempted(),
                    task.getDatumStatusCounter().getSuccess(),
                    task.getDatumStatusCounter().getPartial(),
                    task.getDatumStatusCounter().getFail(),
                    task.getDatumStatusCounter().getEmitted());

            try
            {
                Thread.sleep(seconds*1000);
            }
            catch (InterruptedException e)
            { }
        }
    }

}
