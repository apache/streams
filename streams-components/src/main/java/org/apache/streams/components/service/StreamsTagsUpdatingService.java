package org.apache.streams.components.service;

public interface StreamsTagsUpdatingService {
    String updateTags(String subscriberId, String tagsJson) throws Exception;
}
