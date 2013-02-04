package org.apache.streams.osgi.components.activitysubscriber.impl;


import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscriptionFilter;

public class ActivityStreamsSubscriptionFilterImpl implements ActivityStreamsSubscriptionFilter{

    private String[] fields;
    private String comparisonOperator;
    private String[] valueSet;

    @Override
    public String[] getFields() {
        return fields;
    }

    @Override
    public void setFields(String[] fields) {
       this.fields=fields;
    }

    @Override
    public String getComparisonOperator() {
        return comparisonOperator;
    }

    @Override
    public void setComparisonOperator(String comparisonOperator) {
       this.comparisonOperator=comparisonOperator;
    }

    @Override
    public String[] getValueSet() {
        return valueSet;
    }

    @Override
    public void setValueSet(String[] valueSet) {
        this.valueSet = valueSet;
    }
}
