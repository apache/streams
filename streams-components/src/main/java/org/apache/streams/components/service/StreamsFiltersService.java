package org.apache.streams.components.service;

import org.codehaus.jackson.JsonParseException;

import java.io.IOException;

public interface StreamsFiltersService {
    String getFilters(String subscriberId) throws Exception;
    String updateFilters(String subscriberId, String tagsJson) throws Exception;
}
