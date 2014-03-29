package org.apache.streams.urls;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public interface Link
{
    @JsonProperty("status")
    public LinkStatus getStatus();

    @JsonProperty("originalUrl")
    public String getOriginalURL();

    @JsonProperty("wasRedirected")
    public boolean wasRedirected();

    @JsonProperty("finalUrl")
    public String getFinalURL();

    @JsonProperty("domain")
    public String getDomain();

    @JsonProperty("normalizedUrl")
    public String getNormalizedURL();

    @JsonProperty("urlParts")
    public List<String> getUrlParts();

    @JsonProperty("finalStatusCode")
    public String getFinalResponseCode();

    @JsonProperty("isTracked")
    public boolean isTracked();

    @JsonProperty("redirects")
    public List<String> getRedirects();

    @JsonProperty("tookInMillis")
    public long getTookInMillis();

    public void run();

    public enum LinkStatus {
        SUCCESS,
        ERROR,
        MALFORMED_URL,
        NOT_FOUND,
        FORBIDDEN,
        REDIRECT_ERROR,
        UNAUTHORIZED,
        LOOP,
        HTTP_ERROR_STATUS,
        EXCEPTION
    }

}