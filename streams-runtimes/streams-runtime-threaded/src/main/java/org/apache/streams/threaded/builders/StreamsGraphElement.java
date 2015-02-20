package org.apache.streams.threaded.builders;

public class StreamsGraphElement {

    private String source;
    private String target;
    private String type;
    private int value;

    StreamsGraphElement(String source, String target, String type, int value) {
        this.source = source;
        this.target = target;
        this.type = type;
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    void setTarget(String target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    void setValue(int value) {
        this.value = value;
    }
}
