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

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class HbaseConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(HbaseConfigurator.class);

    public static HbaseConfiguration detectConfiguration() {

        Config zookeeper = StreamsConfigurator.config.getConfig("zookeeper");
        Config hbase = StreamsConfigurator.config.getConfig("hbase");

        String rootdir = hbase.getString("rootdir");

        Config znode = zookeeper.getConfig("znode");

        String rootserver = znode.getString("rootserver");
        String parent = znode.getString("parent");
        Integer clientPort = hbase.getConfig("zookeeper").getConfig("property").getInt("clientPort");
        String quorum = hbase.getConfig("zookeeper").getString("quorum");

        HbaseConfiguration hbaseConfiguration = new HbaseConfiguration();

        hbaseConfiguration.setRootdir(rootdir);
        hbaseConfiguration.setRootserver(rootserver);
        hbaseConfiguration.setParent(parent);
        hbaseConfiguration.setQuorum(quorum);
        hbaseConfiguration.setClientPort(clientPort.longValue());
        hbaseConfiguration.setTable(hbase.getString("table"));
        hbaseConfiguration.setFamily(hbase.getString("family"));
        hbaseConfiguration.setQualifier(hbase.getString("qualifier"));

        return hbaseConfiguration;
    }

}
