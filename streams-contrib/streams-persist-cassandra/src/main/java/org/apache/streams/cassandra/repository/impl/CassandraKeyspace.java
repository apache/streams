package org.apache.streams.cassandra.repository.impl;

/*
 * #%L
 * streams-persist-cassandra [org.apache.streams]
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

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.streams.cassandra.configuration.CassandraConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class CassandraKeyspace {
    private CassandraConfiguration configuration;
    private Cluster cluster;
    private Session session;

    @Autowired
    public CassandraKeyspace(CassandraConfiguration configuration){
        this.configuration = configuration;

        cluster = Cluster.builder().addContactPoint(configuration.getCassandraPort()).build();
        session = cluster.connect();

        //TODO: cassandra 2 will have support for CREATE KEYSPACE IF NOT EXISTS
        try {
            session.execute("CREATE KEYSPACE " + configuration.getKeyspaceName() + " WITH replication = { 'class': 'SimpleStrategy','replication_factor' : 1 };");
        } catch (AlreadyExistsException ignored) {
        }

        //connect to the keyspace
        session = cluster.connect(configuration.getKeyspaceName());
    }

    public Session getSession(){
        return session;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            cluster.shutdown();
        } finally {
            super.finalize();
        }
    }
}
