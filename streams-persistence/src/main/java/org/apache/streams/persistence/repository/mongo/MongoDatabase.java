package org.apache.streams.persistence.repository.mongo;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.persistence.configuration.MongoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class MongoDatabase {
    private Log log = LogFactory.getLog(MongoDatabase.class);
    private DB db;

    @Autowired
    public MongoDatabase(MongoConfiguration configuration) {
        try {
            MongoClient mongoClient = new MongoClient();
            db = mongoClient.getDB(configuration.getDbName());
        } catch (UnknownHostException e) {
            log.error("There was an error connecting to the database", e);
        }
    }

    public DB getDb() {
        return db;
    }

    public void setDb(DB db) {
        this.db = db;
    }
}
