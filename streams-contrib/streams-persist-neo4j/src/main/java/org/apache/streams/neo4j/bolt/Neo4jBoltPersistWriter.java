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

package org.apache.streams.neo4j.bolt;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.neo4j.Neo4jConfiguration;
import org.apache.streams.neo4j.Neo4jPersistUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.javatuples.Pair;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by sblackmon on 12/16/16.
 */
public class Neo4jBoltPersistWriter implements StreamsPersistWriter {

  private Neo4jConfiguration config;

  Neo4jBoltClient client;

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jBoltPersistWriter.class);

  private static ObjectMapper mapper;

  public Neo4jBoltPersistWriter(Neo4jConfiguration config) {
    this.config = config;

  }

  @Override
  public String getId() {
    return Neo4jBoltPersistWriter.class.getSimpleName();
  }

  @Override
  public void prepare(Object configurationObject) {
    client = Neo4jBoltClient.getInstance(config);
  }

  @Override
  public void cleanUp() {
    //
  }

  @Override
  public void write(StreamsDatum entry) {

    List<Pair<String, Map<String, Object>>> statements;
    Session session = null;
    try {
      statements = Neo4jPersistUtil.prepareStatements(entry);
      session = client.client().session();
      Transaction transaction = session.beginTransaction();
      for( Pair<String, Map<String, Object>> statement : statements ) {
        StatementResult statementResult = transaction.run( statement.getValue0(), statement.getValue1() );
        LOGGER.debug("StatementResult", statementResult.single());
      }
      transaction.success();
    } catch( Exception ex ) {
      LOGGER.error("Exception", ex);
    } finally {
      if( session != null ) {
        session.close();
      }
    }
  }


}
