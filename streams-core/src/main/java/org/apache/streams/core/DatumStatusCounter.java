package org.apache.streams.core;

public class DatumStatusCounter
{
    private volatile int success = 0;
    private volatile int fail = 0;
    private volatile int partial = 0;
    private volatile int recordsEmitted = 0;

    public int getSuccess()             { return this.success; }
    public int getFail()                { return  this.fail; }
    public int getPartial()             { return this.partial; }
    public int getEmitted()             { return this.recordsEmitted; }

    public void add(DatumStatusCounter datumStatusCounter) {
        this.success += datumStatusCounter.getSuccess();
        this.partial = datumStatusCounter.getPartial();
        this.fail += datumStatusCounter.getFail();
        this.recordsEmitted += datumStatusCounter.getEmitted();
    }

    public void add(DatumStatus workStatus) {
        // add this to the record counter
        switch(workStatus) {
            case SUCCESS: this.success++; break;
            case PARTIAL: this.partial++; break;
            case FAIL: this.fail++; break;
        }
        this.recordsEmitted += 1;
    }

    @Override
    public String toString() {
        return "DatumStatusCounter{" +
                "success=" + success +
                ", fail=" + fail +
                ", partial=" + partial +
                ", recordsEmitted=" + recordsEmitted +
                '}';
    }
}
