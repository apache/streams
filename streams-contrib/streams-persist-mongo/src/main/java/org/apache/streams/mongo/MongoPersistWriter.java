package org.apache.streams.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MongoPersistWriter implements StreamsPersistWriter, Runnable
{
    private final static Logger LOGGER = LoggerFactory.getLogger(MongoPersistWriter.class);

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private MongoConfiguration config;

    protected DB client;
    protected DBAddress dbaddress;
    protected DBCollection collection;

    protected List<DBObject> insertBatch = new ArrayList<DBObject>();

    public MongoPersistWriter() {
        Config config = StreamsConfigurator.config.getConfig("mongo");
        this.config = MongoConfigurator.detectConfiguration(config);
        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public MongoPersistWriter(Queue<StreamsDatum> persistQueue) {
        Config config = StreamsConfigurator.config.getConfig("mongo");
        this.config = MongoConfigurator.detectConfiguration(config);
        this.persistQueue = persistQueue;
    }

    private synchronized void connectToMongo()
    {
        try {
            dbaddress = new DBAddress(config.getHost(), config.getPort().intValue(), config.getDb());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        client = MongoClient.connect(dbaddress);

        if( !Strings.isNullOrEmpty(config.getUser()) && !Strings.isNullOrEmpty(config.getPassword()))
            client.authenticate(config.getUser(), config.getPassword().toCharArray());

        if( !client.collectionExists(config.getCollection())) {
            client.createCollection(config.getCollection(), null);
        };

        collection = client.getCollection(config.getCollection());
    }
    
    @Override
    public void write(StreamsDatum streamsDatum) {

        DBObject dbObject;
        if( streamsDatum.getDocument() instanceof String ) {
            dbObject = (DBObject) JSON.parse((String)streamsDatum.getDocument());
        } else {
            try {
                ObjectNode node = mapper.valueToTree(streamsDatum.getDocument());
                dbObject = (DBObject) JSON.parse(node.toString());
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn("Unsupported type: " + streamsDatum.getDocument().getClass());
                return;
            }
        }

        insertBatch.add(dbObject);

        if( insertBatch.size() % 100 == 0)
            try {
                flush();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
    }

    public void flush() throws IOException
    {
        collection.insert(insertBatch);
    }

    public synchronized void close() throws IOException
    {
        client.cleanCursors(true);
    }

    public void start() {

        connectToMongo();

    }

    public void stop() {

        try {
            flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public Queue<StreamsDatum> getPersistQueue() {
        return persistQueue;
    }

    public void run() {

        while(true) {
            if( persistQueue.peek() != null ) {
                try {
                    StreamsDatum entry = persistQueue.remove();
                    write(entry);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(new Random().nextInt(1));
            } catch (InterruptedException e) {}
        }

    }

    @Override
    public void prepare(Object configurationObject) {
        start();
    }

    @Override
    public void cleanUp() {
        stop();
    }
}
