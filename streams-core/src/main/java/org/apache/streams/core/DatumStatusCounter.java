package org.apache.streams.core;

public class DatumStatusCounter
{
    private volatile int attempted = 0;
    private volatile int success = 0;
    private volatile int fail = 0;
    private volatile int partial = 0;
    private volatile int emitted = 0;

    public int getAttempted()             { return this.attempted; }
    public int getSuccess()             { return this.success; }
    public int getFail()                { return  this.fail; }
    public int getPartial()             { return this.partial; }
    public int getEmitted()             { return this.emitted; }

    public DatumStatusCounter() {
    }

    public void add(DatumStatusCounter datumStatusCounter) {
        this.attempted += datumStatusCounter.getAttempted();
        this.success += datumStatusCounter.getSuccess();
        this.partial = datumStatusCounter.getPartial();
        this.fail += datumStatusCounter.getFail();
        this.emitted += datumStatusCounter.getEmitted();
    }

    public void incrementAttempt() {
        this.attempted += 1;
    }

    public void incrementAttempt(int counter) {
        this.attempted += counter;
    }

    public synchronized void incrementStatus(DatumStatus workStatus) {
        // add this to the record counter
        switch(workStatus) {
            case SUCCESS: this.success++; break;
            case PARTIAL: this.partial++; break;
            case FAIL: this.fail++; break;
        }
        this.emitted += 1;
    }

    public synchronized void incrementStatus(DatumStatus workStatus, int counter) {
        // add this to the record counter
        switch(workStatus) {
            case SUCCESS: this.success += counter; break;
            case PARTIAL: this.partial += counter; break;
            case FAIL: this.fail += counter; break;
        }
        this.emitted += counter;
    }

    @Override
    public String toString() {
        return "DatumStatusCounter{" +
                "attempted=" + attempted +
                ", success=" + success +
                ", fail=" + fail +
                ", partial=" + partial +
                ", emitted=" + emitted +
                '}';
    }
}
