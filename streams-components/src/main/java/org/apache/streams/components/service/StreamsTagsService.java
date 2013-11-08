package org.apache.streams.components.service;

import org.codehaus.jackson.JsonParseException;

import java.io.IOException;

public interface StreamsTagsService {
    String getTags(String subscriberId) throws Exception;
    String updateTags(String subscriberId, String tagsJson) throws Exception;
}
