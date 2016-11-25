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

package org.apache.streams.util.schema;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

import java.net.URI;
import java.util.Comparator;
import java.util.Iterator;

/**
 * A SchemaStore resolves and indexes json schemas and makes their properties available.
 *
 * <p/>
 * Implementations include
 * - SchemaStoreImpl
 */
public interface SchemaStore extends Comparator<Schema> {

  Schema create(URI uri);

  Schema create(Schema parent, String path);

  void clearCache();

  Integer getSize();

  Optional<Schema> getById(URI id);

  Optional<Schema> getByUri(URI uri);

  Integer getFileUriCount();

  Integer getHttpUriCount();

  Iterator<Schema> getSchemaIterator();

  ObjectNode resolveProperties(Schema schema, ObjectNode fieldNode, String resourceId);

  ObjectNode resolveItems(Schema schema, ObjectNode fieldNode, String resourceId);

  @Override
  int compare(Schema left, Schema right);
}
