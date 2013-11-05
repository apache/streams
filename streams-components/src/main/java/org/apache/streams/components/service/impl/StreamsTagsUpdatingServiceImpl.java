package org.apache.streams.components.service.impl;

import org.apache.streams.components.service.StreamsSubscriptionRepositoryService;
import org.apache.streams.components.service.StreamsTagsUpdatingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamsTagsUpdatingServiceImpl implements StreamsTagsUpdatingService {

    private StreamsSubscriptionRepositoryService repositoryService;
    private ObjectMapper mapper;

    @Autowired
    public StreamsTagsUpdatingServiceImpl(StreamsSubscriptionRepositoryService repositoryService, ObjectMapper mapper) {
        this.repositoryService = repositoryService;
        this.mapper = mapper;
    }

    @Override
    public String updateTags(String tagsJson) {
        //TODO:implelement this!!!
        return "Tags Updated Successfully!";
    }
}
