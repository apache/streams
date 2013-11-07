package org.apache.streams.persistence.model;

public interface ActivityStreamsObject {
    String getDisplayName();
    void setDisplayName(String displayName);
    String getObjectType();
    void setObjectType(String objectType);
    String getId();
    void setId(String id);
    String getUrl();
    void setUrl(String url);
}
