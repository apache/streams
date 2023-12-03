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

package org.apache.streams.util.schema.test;

import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.util.schema.Schema;
import org.apache.streams.util.schema.SchemaStore;
import org.apache.streams.util.schema.SchemaStoreImpl;

import com.typesafe.config.Config;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

/**
 * Unit Tests for SchemaStore.
 */
public class SchemaStoreTest {

  Config testconfig = StreamsConfigurator.getConfig().getConfig(SchemaStoreTest.class.getSimpleName());

  @Test
  public void indexMediaLink() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File file = new File(testconfig.getString("medialinkSchema"));
    schemaStore.create(file.toURI());
    assert ( schemaStore.getFileUriCount() == 1);
    assert ( schemaStore.getByUri(file.toURI()).isPresent());
    assert ( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
  }

  @Test
  public void indexApprove() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File file = new File(testconfig.getString("approveSchema"));
    schemaStore.create(file.toURI());
    assert ( schemaStore.getFileUriCount() == 4);
    assert ( schemaStore.getByUri(file.toURI()).isPresent());
    assert ( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
  }

  @Test
  @Ignore
  public void indexCollection() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File file = new File(testconfig.getString("collectionSchema"));
    schemaStore.create(file.toURI());
    assert ( schemaStore.getFileUriCount() == 3);
    assert ( schemaStore.getByUri(file.toURI()).isPresent());
    assert ( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    Schema collection = schemaStore.getByUri(file.toURI()).get();
    assert ( collection.getParent() == null );
  }

  @Test
  public void indexUpdate() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File file = new File(testconfig.getString("updateSchema"));
    schemaStore.create(file.toURI());
    assert ( schemaStore.getFileUriCount() == 4);
    assert ( schemaStore.getByUri(file.toURI()).isPresent());
    assert ( schemaStore.getById(schemaStore.getByUri(file.toURI()).get().getId()).isPresent());
    Schema update = schemaStore.getByUri(file.toURI()).get();
    assert ( update.getParent() != null );
    File parentFile = new File(testconfig.getString("activitySchema"));
    Schema parent = schemaStore.getByUri(parentFile.toURI()).get();
    assert ( parent != null );
    assert ( update.getParentUri().equals(parent.getUri()));
  }

  // test create from messed up URI

  // test create from URI with messed up reference

}
