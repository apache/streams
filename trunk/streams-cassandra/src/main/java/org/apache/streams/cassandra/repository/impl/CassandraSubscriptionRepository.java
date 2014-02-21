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

package org.apache.streams.cassandra.repository.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.streams.cassandra.configuration.CassandraConfiguration;
import org.apache.streams.osgi.components.activitysubscriber.ActivityStreamsSubscription;
import org.springframework.beans.factory.annotation.Autowired;

public class CassandraSubscriptionRepository {
    private static final Log LOG = LogFactory.getLog(CassandraSubscriptionRepository.class);

    private CassandraKeyspace keyspace;
    private CassandraConfiguration configuration;

    @Autowired
    public CassandraSubscriptionRepository(CassandraKeyspace keyspace, CassandraConfiguration configuration) {
        this.keyspace = keyspace;
        this.configuration = configuration;

        try {
            keyspace.getSession().execute("CREATE TABLE " + configuration.getSubscriptionColumnFamilyName() + " (" +
                    "id text, " +
                    "filters text, " +

                    "PRIMARY KEY (id));");
        } catch (AlreadyExistsException ignored) {
        }
    }

    public String getFilters(String id){
        String cql = "SELECT * FROM " + configuration.getSubscriptionColumnFamilyName()  + " WHERE id = '" + id+"';";

        ResultSet set = keyspace.getSession().execute(cql);

        return set.one().getString("filters");
    }

    public void save(ActivityStreamsSubscription subscription){
        String cql = "INSERT INTO " + configuration.getSubscriptionColumnFamilyName()  + " (" +
                "id, filters) " +
                "VALUES ('" +
                subscription.getAuthToken() + "','" +
                StringUtils.join(subscription.getFilters(), " ") +

                "')";
        keyspace.getSession().execute(cql);
    }
}
