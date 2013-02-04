package org.apache.streams.osgi.components.activitysubscriber;


public interface ActivityStreamsSubscriptionFilter {

    public String[] getFields();
    public void setFields(String[] fields);

    public String getComparisonOperator();
    public void setComparisonOperator(String comparisonOperator);

    public String[] getValueSet();
    public void setValueSet(String[] valueSet);

}
