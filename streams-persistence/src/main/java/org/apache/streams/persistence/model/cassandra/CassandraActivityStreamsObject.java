package org.apache.streams.persistence.model.cassandra;

import org.apache.streams.persistence.model.ActivityStreamsMediaLink;
import org.apache.streams.persistence.model.ActivityStreamsObject;

public class CassandraActivityStreamsObject implements ActivityStreamsObject {
    private String displayName;
    private String id;
    private String objectType;
    private String url;
    private ActivityStreamsMediaLink image;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

	@Override
	public ActivityStreamsMediaLink getImage() {
		return image;
	}

	@Override
	public void setImage(ActivityStreamsMediaLink image) {
		this.image = image;		
	}
}
