package org.apache.streams.osgi.components.activitysubscriber;

public interface ActivityStreamsSubscriptionOutput {

    public String getOutputType();
    public void setOutputType(String outputType);

    public String getMethod();
    public void setMethod(String method);

    public String getUrl();
    public void setUrl(String url);

    public String getDeliveryFrequency();
    public void setDeliveryFrequency(String deliveryFrequency);

    public String getMaxSize();
    public void setMaxSize(int maxSize);

    public String getAuthType();
    public void setAuthType(String authType);

    public String getUsername();
    public void setUsername(String username);

    public String getPassword();
    public void setPassword(String password);

}
