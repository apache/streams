package org.apache.streams.persistence.model;

public interface ActivityStreamsPublisher {
    String getInRoute();
    String getId();
    String getSrc();
    void setInRoute(String inRoute);
    void setId(String id);
    void setSrc(String src);
}
