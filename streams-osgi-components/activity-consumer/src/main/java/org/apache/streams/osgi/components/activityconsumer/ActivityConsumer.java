package org.apache.streams.osgi.components.activityconsumer;


public interface ActivityConsumer {
    public void receive(String activity);
    public void init();
    public String getSrc();
    public void setInRoute(String route);
    public String getInRoute();
}
