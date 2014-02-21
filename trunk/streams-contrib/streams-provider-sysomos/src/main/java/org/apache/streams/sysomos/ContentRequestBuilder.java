package org.apache.streams.sysomos;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is the best class to use when syncing information between sysomos and our data store.
 */
public class ContentRequestBuilder extends RequestBuilder {

    private String baseUrl;
    private String hid;
    private String addedAfter;
    private String addedBefore;
    private String size;
    private String offset;
    private String apiKey;

    /**
     * The max number of items you are allowed to get per request.
     */
    public static final int MAX_ALLOWED_PER_REQUEST = 10000;

    protected ContentRequestBuilder(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    protected URL getFullRequestUrl() throws SysomosException, MalformedURLException {
        StringBuilder url = new StringBuilder();
        url.append(this.baseUrl);
        url.append("dev/v1/heartbeat/content?");
        url.append("apiKey=");
        url.append(this.apiKey);
        url.append("&hid=");
        url.append(this.hid);
        if (size != null) {
            url.append("&size=");
            url.append(this.size);
        }
        if (this.addedAfter != null) {
            url.append("&addedAfter=");
            url.append(this.addedAfter);
        }
        if (this.addedBefore != null) {
            url.append("&addedBefore=");
            url.append(this.addedBefore);
        }
        if (this.offset != null) {
            url.append("&offset=");
            url.append(this.offset);
        }
        return new URL(url.toString());  //To change body of implemented methods use File | Settings | File Templates.
    }


    public ContentRequestBuilder setHeartBeatId(int hid) {
        return setHeartBeatId(Integer.toString(hid));
    }

    public ContentRequestBuilder setHeartBeatId(String hid) {
        this.hid = hid;
        return this;
    }

    public ContentRequestBuilder setAddedAfterDate(String dateString) {
        this.addedAfter = dateString;
        return this;
    }

    public ContentRequestBuilder setAddedBeforeDate(String dateString) {
        this.addedBefore = dateString;
        return this;
    }

    public ContentRequestBuilder setReturnSetSize(int size) {
        this.size = Integer.toString(Math.min(size, MAX_ALLOWED_PER_REQUEST));
        return this;
    }

    public ContentRequestBuilder setOffset(int offset) {
        this.offset = Integer.toString(offset);
        return this;
    }

    public String getURLString() {
        try {
            return getFullRequestUrl().toString();
        } catch (Exception e) {
            return "";
        }
    }

}
