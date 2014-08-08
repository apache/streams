package org.apache.streams.datasift.provider;

import com.datasift.client.DataSiftClient;
import com.datasift.client.FutureData;
import com.datasift.client.managedsource.ManagedSource;
import com.datasift.client.managedsource.ManagedSourceList;
import com.datasift.client.managedsource.sources.DataSource;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.datasift.DatasiftConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by sblackmon on 8/8/14.
 */
public class DatasiftManagedSourceSetup implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftStreamProvider.class);

    private static DatasiftConfiguration config = DatasiftStreamConfigurator.detectConfiguration(StreamsConfigurator.config);

    DataSiftClient client;
    Map<String, ManagedSource> currentManagedSourceMap = Maps.newHashMap();
    List<ManagedSource> updatedManagedSourceList;

    public static void main(String[] args) {
        DatasiftManagedSourceSetup job = new DatasiftManagedSourceSetup();
        (new Thread(job)).start();
    }

    @Override
    public void run() {

        setup();

        current();

        updatedManagedSourceList = config.getManagedSources();

        for( ManagedSource source : updatedManagedSourceList ) {
            ManagedSource current = currentManagedSourceMap.get( source.getId() );
            ManagedSource updated = client.managedSource().update(current.getName(), (DataSource) source, current).sync();
        }

    }

    public void setup() {
        client = DatasiftStreamProvider.getNewClient(config.getUserName(), config.getApiKey());
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
