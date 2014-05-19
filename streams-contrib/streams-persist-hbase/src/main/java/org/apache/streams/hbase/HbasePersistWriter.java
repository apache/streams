package org.apache.streams.hbase;

/*
 * #%L
 * streams-persist-hbase
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.*;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HbasePersistWriter implements StreamsPersistWriter, Flushable, Closeable
{
    private final static Logger LOGGER = LoggerFactory.getLogger(HbasePersistWriter.class);

    protected HConnection connection;
    protected HTablePool pool;
    protected HTableInterface table;
    protected HTableDescriptor descriptor;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    private HbaseConfiguration config;

    public HbasePersistWriter() {
        this.config = HbaseConfigurator.detectConfiguration();
        this.persistQueue  = new ConcurrentLinkedQueue<StreamsDatum>();
    }

    public HbasePersistWriter(Queue<StreamsDatum> persistQueue) {
        this.config = HbaseConfigurator.detectConfiguration();
        this.persistQueue = persistQueue;
    }

    private synchronized void connectToHbase()
    {
        Configuration configuration = new Configuration();
        configuration.set("hbase.rootdir", config.getRootdir());
        configuration.set("zookeeper.znode.parent", config.getParent());
        configuration.set("zookeeper.znode.rootserver", config.getRootserver());
        //configuration.set("hbase.master", config.getRootserver());
        //configuration.set("hbase.cluster.distributed", "false");
        configuration.set("hbase.zookeeper.quorum", config.getQuorum());
        configuration.set("hbase.zookeeper.property.clientPort", Long.toString(config.getClientPort()));
        configuration.setInt("zookeeper.session.timeout", 1000);

        configuration.setInt("timeout", 1000);

        //pool = new HTablePool(configuration, 10);
        try {
            connection = HConnectionManager.createConnection(configuration);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
        //    table = new HTable(configuration, config.getTable());
        //    table = (HTable) pool.getTable(config.getTable());
            table = new HTable(configuration, config.getTable().getBytes());
            table.setAutoFlush(true);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //

        try {
            descriptor = table.getTableDescriptor();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try
        {
            LOGGER.info("Table : {}", descriptor);
        }
        catch (Exception e)
        {
            LOGGER.error("There was an error connecting to HBase, please check your settings and try again");
            e.printStackTrace();
            return;
        }
    }
    
    @Override
    public void write(StreamsDatum streamsDatum) {

        ObjectNode node;
        Put put = new Put();
        if( streamsDatum.getDocument() instanceof String ) {
            try {
                node = mapper.readValue((String)streamsDatum.getDocument(), ObjectNode.class);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Invalid json: {}", streamsDatum.getDocument().toString());
                return;
            }
            put.setId(GuidUtils.generateGuid(node.toString()));
            try {
                byte[] value = node.binaryValue();
                put.add(config.getFamily().getBytes(), config.getQualifier().getBytes(), value);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Failure adding object: {}", streamsDatum.getDocument().toString());
                return;
            }
        } else {
            try {
                node = mapper.valueToTree(streamsDatum.getDocument());
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn("Invalid json: {}", streamsDatum.getDocument().toString());
                return;
            }
            put.setId(GuidUtils.generateGuid(node.toString()));
            try {
                byte[] value = node.binaryValue();
                put.add(config.getFamily().getBytes(), config.getQualifier().getBytes(), value);
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Failure preparing put: {}", streamsDatum.getDocument().toString());
                return;
            }
        }
        try {
            table.put(put);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Failure executin put: {}", streamsDatum.getDocument().toString());
            return;
        }

    }

    public void flush() throws IOException
    {
        table.flushCommits();
    }

    public synchronized void close() throws IOException
    {
        table.close();
    }

    @Override
    public void prepare(Object configurationObject) {

        connectToHbase();

        Thread task = new Thread(new HbasePersistWriterTask(this));
        task.start();

        try {
            task.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

    }

    @Override
    public void cleanUp() {

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
}
