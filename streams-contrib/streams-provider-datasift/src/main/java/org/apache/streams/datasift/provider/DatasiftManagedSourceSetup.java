/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.datasift.provider;

import com.datasift.client.DataSiftClient;
import com.datasift.client.managedsource.ManagedSource;
import com.datasift.client.managedsource.ManagedSourceList;
import com.datasift.client.managedsource.sources.DataSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.apache.streams.datasift.managed.StreamsManagedSource;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 8/8/14.
 */
public class DatasiftManagedSourceSetup implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamProvider.class);

    private static DatasiftConfiguration config = DatasiftStreamConfigurator.detectConfiguration(StreamsConfigurator.config);

    private static final ObjectMapper MAPPER = StreamsDatasiftMapper.getInstance();

    DataSiftClient client;
    Map<String, ManagedSource> currentManagedSourceMap = Maps.newHashMap();
    List<StreamsManagedSource> updatedManagedSourceList;

    public static void main(String[] args) {
        DatasiftManagedSourceSetup job = new DatasiftManagedSourceSetup();
        (new Thread(job)).start();
    }

    @Override
    public void run() {

        setup();

        current();

        updatedManagedSourceList = config.getManagedSources();

        for( StreamsManagedSource source : updatedManagedSourceList ) {
            ManagedSource current = currentManagedSourceMap.get( source.getId() );
            LOGGER.info( "CURRENT: " + current );
            // merge 'em
            ManagedSource working = MAPPER.convertValue(source, ManagedSource.class);
            LOGGER.info( "WORKING: " + working );
            ManagedSource updated = client.managedSource().update(current.getName(), (DataSource) working, current).sync();
            LOGGER.info( "UPDATED: " + updated );

        }

    }

    public void setup() {

        client = new DatasiftStreamProvider(null, config).getNewClient(config.getUserName(), config.getApiKey());
    }

    public void current() {
        ManagedSourceList managedSources = client.managedSource().get().sync();
        Iterator<ManagedSource> managedSourceIterator = managedSources.iterator();
        while( managedSourceIterator.hasNext() ) {
            ManagedSource source = managedSourceIterator.next();
            currentManagedSourceMap.put(source.getId(), source);
        }
    }

}
