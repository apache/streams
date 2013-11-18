package org.apache.streams.persistence.model.mongo;

import com.mongodb.BasicDBObject;
import org.apache.streams.persistence.model.ActivityStreamsMediaLink;


public class MongoActivityStreamsMediaLink extends BasicDBObject implements ActivityStreamsMediaLink{

	@Override
	public String getUrl() {
		return (String)get("url");
	}

	@Override
	public void setUrl(String url) {
		put("url",url);
		
	}

}
