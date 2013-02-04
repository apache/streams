package org.apache.streams.osgi.components.activityconsumer;


public interface ActivityConsumer {
    public String receive(String activity);
    public void init();
    public String getSrc();
    public void setInRoute(String route);
    public String getInRoute();
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
}
