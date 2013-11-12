package org.apache.streams.components.service;

public interface StreamsActivityReceivingService {
    String getActivity(String subscriberID) throws Exception;
}
