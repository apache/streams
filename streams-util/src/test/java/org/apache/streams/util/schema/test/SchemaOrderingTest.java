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

import org.apache.streams.util.schema.Schema;
import org.apache.streams.util.schema.SchemaStore;
import org.apache.streams.util.schema.SchemaStoreImpl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sblackmon on 5/3/16.
 */
public class SchemaOrderingTest {

  @Test
  public void compareVerbParent() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File update = new File("target/test-classes/activitystreams-schemas/verbs/update.json");
    schemaStore.create(update.toURI());
    File activity = new File("target/test-classes/activitystreams-schemas/activity.json");
    schemaStore.create(activity.toURI());
    assert ( schemaStore.compare( schemaStore.getByUri(update.toURI()).get(), schemaStore.getByUri(activity.toURI()).get()) == 1);
    Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
    assertContainsItemsEndingWithInOrder(
        schemaIterator,
        Lists.newArrayList(
            "activity.json",
            "update.json"
        )
    );
  }

  @Test
  public void compareObjectTypeParent() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File alert = new File("target/test-classes/activitystreams-schemas/objectTypes/alert.json");
    schemaStore.create(alert.toURI());
    File object = new File("target/test-classes/activitystreams-schemas/object.json");
    schemaStore.create(object.toURI());
    assert ( schemaStore.compare( schemaStore.getByUri(object.toURI()).get(), schemaStore.getByUri(alert.toURI()).get()) == -1);
    Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
    assertContainsItemsEndingWithInOrder(
        schemaIterator,
        Lists.newArrayList(
            "object.json",
            "alert.json"
        )
    );
  }

  @Test
  public void compareUnrelated() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File alert = new File("target/test-classes/activitystreams-schemas/objectTypes/alert.json");
    schemaStore.create(alert.toURI());
    File update = new File("target/test-classes/activitystreams-schemas/verbs/update.json");
    schemaStore.create(update.toURI());
    assert ( schemaStore.compare( schemaStore.getByUri(alert.toURI()).get(), schemaStore.getByUri(update.toURI()).get()) == 0);
  }

  @Test
  public void compareVerbFieldRef() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File update = new File("target/test-classes/activitystreams-schemas/verbs/update.json");
    schemaStore.create(update.toURI());
    File object = new File("target/test-classes/activitystreams-schemas/object.json");
    schemaStore.create(object.toURI());
    assert ( schemaStore.compare( schemaStore.getByUri(update.toURI()).get(), schemaStore.getByUri(object.toURI()).get()) == 1);
    Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
    assertContainsItemsEndingWithInOrder(
        schemaIterator,
        Lists.newArrayList(
            "object.json",
            "update.json"
        )
    );
  }

  @Test
  public void compareObjectTypeFieldRef() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File alert = new File("target/test-classes/activitystreams-schemas/objectTypes/alert.json");
    schemaStore.create(alert.toURI());
    File mediaLink = new File("target/test-classes/activitystreams-schemas/media_link.json");
    schemaStore.create(mediaLink.toURI());
    assert ( schemaStore.compare( schemaStore.getByUri(mediaLink.toURI()).get(), schemaStore.getByUri(alert.toURI()).get()) == -1);
    Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
    assertContainsItemsEndingWithInOrder(
        schemaIterator,
        Lists.newArrayList(
            "media_link.json",
            "object.json",
            "alert.json"
        )
    );
  }

  @Test
  public void compareVerbAncestorIndirect() {
    SchemaStore schemaStore = new SchemaStoreImpl();
    File update = new File("target/test-classes/activitystreams-schemas/verbs/update.json");
    schemaStore.create(update.toURI());
    File mediaLink = new File("target/test-classes/activitystreams-schemas/media_link.json");
    schemaStore.create(mediaLink.toURI());
    assert ( schemaStore.getByUri(mediaLink.toURI()).isPresent());
    assert ( schemaStore.getByUri(update.toURI()).isPresent());
    assert ( schemaStore.compare( schemaStore.getByUri(mediaLink.toURI()).get(), schemaStore.getByUri(update.toURI()).get()) == -1);
    Iterator<Schema> schemaIterator = schemaStore.getSchemaIterator();
    assertContainsItemsEndingWithInOrder(
        schemaIterator,
        Lists.newArrayList(
            "media_link.json",
            "update.json"
        )
    );
  }

  /**
   * assert iterator of Schema contains URI items ending with in order.
   * @param iterator Iterator of Schema
   * @param items List of String
   */
  public void assertContainsItemsEndingWithInOrder(Iterator<Schema> iterator, List<String> items) {
    for ( String item : items ) {
      Optional<Schema> tryFind = Iterators.tryFind( iterator, new SchemaUriEndsWithPredicate(item) );
      assert ( tryFind.isPresent() );
    }
  }

  public class SchemaUriEndsWithPredicate implements Predicate<Schema> {

    private String endsWith;

    public SchemaUriEndsWithPredicate(String endsWith) {
      this.endsWith = endsWith;
    }

    @Override
    public boolean apply(Schema input) {
      return input.getUri().getPath().endsWith(endsWith);
    }

    @Override
    public boolean equals(Object object) {
      return false;
    }
  }
}
