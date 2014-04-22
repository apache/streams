package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class PercolateProcessor implements StreamsProcessor, Runnable {
    private final static Logger LOGGER = LoggerFactory.getLogger(PercolateProcessor.class);

    private ObjectMapper mapper = new ObjectMapper();

    protected Queue<StreamsDatum> inQueue;
    protected Queue<StreamsDatum> outQueue;

    private ElasticsearchWriterConfiguration config;
    private ElasticsearchClientManager manager;

    public PercolateProcessor(Queue<StreamsDatum> inQueue) {
        this.inQueue = inQueue;
        this.outQueue = new LinkedBlockingQueue<StreamsDatum>();
    }

    public ElasticsearchClientManager getManager() {
        return manager;
    }

    public void setManager(ElasticsearchClientManager manager) {
        this.manager = manager;
    }

    public ElasticsearchWriterConfiguration getConfig() {
        return config;
    }

    public void setConfig(ElasticsearchWriterConfiguration config) {
        this.config = config;
    }

    public void start() {
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(manager);
        Preconditions.checkNotNull(manager.getClient());
    }

    public void stop() {

    }

    public Queue<StreamsDatum> getProcessorOutputQueue() {
        return outQueue;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        String json;
        ObjectNode node;
        // first check for valid json
        if (entry.getDocument() instanceof String) {
            json = (String) entry.getDocument();
            try {
                node = (ObjectNode) mapper.readTree(json);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            node = (ObjectNode) entry.getDocument();
            json = node.asText();
        }

        PercolateResponse response = manager.getClient().preparePercolate().setDocumentType(config.getType()).setSource(json).execute().actionGet();

        ArrayNode tagArray = JsonNodeFactory.instance.arrayNode();

        for (PercolateResponse.Match match : response.getMatches()) {
            tagArray.add(match.getId().string());

        }

        // need utility methods for get / create specific node
        ObjectNode extensions = (ObjectNode) node.get("extensions");
        ObjectNode w2o = (ObjectNode) extensions.get("w2o");
        w2o.put("tags", tagArray);

        result.add(entry);

        return result;

    }

    @Override
    public void prepare(Object o) {
        start();
    }

    @Override
    public void cleanUp() {
        stop();
    }

    @Override
    public void run() {

        while (true) {
            StreamsDatum item;
            try {
                item = inQueue.poll();

                Thread.sleep(new Random().nextInt(100));

                for (StreamsDatum entry : process(item)) {
                    outQueue.offer(entry);
                }


            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }
}