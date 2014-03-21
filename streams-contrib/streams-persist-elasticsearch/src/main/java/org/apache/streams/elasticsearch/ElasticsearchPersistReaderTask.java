package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.core.StreamsDatum;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public class ElasticsearchPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistReaderTask.class);

    private ElasticsearchPersistReader reader;

    private ObjectMapper mapper = new ObjectMapper();

    public ElasticsearchPersistReaderTask(ElasticsearchPersistReader reader) {
        this.reader = reader;
    }

    @Override
    public void run() {

        StreamsDatum item;
        while( reader.hasNext()) {
            SearchHit hit = reader.next();
            ObjectNode jsonObject = null;
            try {
                jsonObject = mapper.readValue(hit.getSourceAsString(), ObjectNode.class);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            item = new StreamsDatum(jsonObject);
            item.getMetadata().put("id", hit.getId());
            item.getMetadata().put("index", hit.getIndex());
            item.getMetadata().put("type", hit.getType());
            reader.persistQueue.offer(item);
        }
        try {
            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException e) {}

    }

}
