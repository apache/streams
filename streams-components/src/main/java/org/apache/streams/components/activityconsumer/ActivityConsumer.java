package org.apache.streams.components.activityconsumer;


import org.codehaus.jackson.annotate.JsonTypeInfo;

import java.net.URI;


@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public interface ActivityConsumer {
    public String receive(String activity);
    public void init();
    public URI getSrc();
    public void setSrc(String src);
    public void setInRoute(String route);
    public String getInRoute();
    public String getAuthToken();
    public void setAuthToken(String token);
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
}
