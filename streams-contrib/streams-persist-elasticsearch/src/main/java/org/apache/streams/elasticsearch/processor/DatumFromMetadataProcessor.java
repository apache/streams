package org.apache.streams.elasticsearch.processor;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sblackmon on 9/4/14.
 */
public class DatumFromMetadataProcessor implements StreamsProcessor, Serializable {

    public final static String STREAMS_ID = "DatumFromMetadataProcessor";

    private ElasticsearchClientManager elasticsearchClientManager;
    private ElasticsearchReaderConfiguration config;

    public DatumFromMetadataProcessor() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DatumFromMetadataProcessor(Config config) {
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public DatumFromMetadataProcessor(ElasticsearchReaderConfiguration config) {
        this.config = config;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        List<StreamsDatum> result = Lists.newArrayList();

        if(entry == null || entry.getMetadata() == null)
            return result;

        String index = (String) entry.getMetadata().get("index");
        String type = (String) entry.getMetadata().get("type");
        String id = (String) entry.getMetadata().get("id");

        if( index == null ) {
            index = this.config.getIndexes().get(0);
        }
        if( type == null ) {
            type = this.config.getTypes().get(0);
        }
        if( id == null ) {
            id = entry.getId();
        }

        GetRequestBuilder getRequestBuilder = elasticsearchClientManager.getClient().prepareGet(index, type, id);
        getRequestBuilder.setFields("*", "_timestamp");
        getRequestBuilder.setFetchSource(true);
        GetResponse getResponse = getRequestBuilder.get();

        if( getResponse == null || getResponse.isExists() == false || getResponse.isSourceEmpty() == true )
            return result;

        entry.setDocument(getResponse.getSource());
        if( getResponse.getField("_timestamp") != null) {
            DateTime timestamp = new DateTime(((Long) getResponse.getField("_timestamp").getValue()).longValue());
            entry.setTimestamp(timestamp);
        }

        result.add(entry);

        return result;
    }

    @Override
    public void prepare(Object configurationObject) {
        this.elasticsearchClientManager = new ElasticsearchClientManager(config);

    }

    @Override
    public void cleanUp() {
        this.elasticsearchClientManager.getClient().close();
    }
}
