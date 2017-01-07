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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Ordering;
import org.apache.commons.lang3.StringUtils;
import org.jsonschema2pojo.ContentResolver;
import org.jsonschema2pojo.FragmentResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.streams.util.schema.UriUtil.safeResolve;

/**
 * Default Implementation of SchemaStore.
 */
public class SchemaStoreImpl extends Ordering<Schema> implements SchemaStore {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaStore.class);
  private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

  protected Map<URI, Schema> schemas = new HashMap<>();
  protected FragmentResolver fragmentResolver = new FragmentResolver();
  protected ContentResolver contentResolver = new ContentResolver();

  public SchemaStoreImpl() {
  }

  @Override
  public synchronized Schema create(URI uri) {
    if (!getByUri(uri).isPresent()) {
      URI baseUri = UriUtil.removeFragment(uri);
      JsonNode baseNode = this.contentResolver.resolve(baseUri);
      if (uri.toString().contains("#") && !uri.toString().endsWith("#")) {
        Schema newSchema = new Schema(baseUri, baseNode, null, true);
        this.schemas.put(baseUri, newSchema);
        JsonNode childContent = this.fragmentResolver.resolve(baseNode, '#' + StringUtils.substringAfter(uri.toString(), "#"));
        this.schemas.put(uri, new Schema(uri, childContent, newSchema, false));
      } else {
        if ( baseNode.has("extends") && baseNode.get("extends").isObject()) {
          URI ref = URI.create((baseNode.get("extends")).get("$ref").asText());
          URI absoluteUri;
          if ( ref.isAbsolute()) {
            absoluteUri = ref;
          } else {
            absoluteUri = baseUri.resolve(ref);
          }
          JsonNode parentNode = this.contentResolver.resolve(absoluteUri);
          Schema parentSchema;
          if ( this.schemas.get(absoluteUri) != null ) {
            parentSchema = this.schemas.get(absoluteUri);
          } else {
            parentSchema = create(absoluteUri);
          }
          this.schemas.put(uri, new Schema(uri, baseNode, parentSchema, true));
        } else {
          this.schemas.put(uri, new Schema(uri, baseNode, null, true));
        }
      }
      List<JsonNode> refs = baseNode.findValues("$ref");
      for ( JsonNode ref : refs ) {
        if ( ref.isValueNode() ) {
          String refVal = ref.asText();
          URI refUri = null;
          try {
            refUri = URI.create(refVal);
          } catch ( Exception ex ) {
            LOGGER.info("Exception: {}", ex.getMessage());
          }
          if (refUri != null && !getByUri(refUri).isPresent()) {
            if (refUri.isAbsolute()) {
              create(refUri);
            } else {
              create(baseUri.resolve(refUri));
            }
          }
        }
      }
    }

    return this.schemas.get(uri);
  }

  @Override
  public Schema create(Schema parent, String path) {
    if (path.equals("#")) {
      return parent;
    } else {
      path = StringUtils.stripEnd(path, "#?&/");
      URI id = (parent != null && parent.getId() != null)
          ? parent.getId().resolve(path)
          : URI.create(path);
      if (this.selfReferenceWithoutParentFile(parent, path)) {
        this.schemas.put(id, new Schema(id, this.fragmentResolver.resolve(parent.getParentContent(), path), parent, false));
        return this.schemas.get(id);
      } else {
        return this.create(id);
      }
    }
  }

  protected boolean selfReferenceWithoutParentFile(Schema parent, String path) {
    return parent != null && (parent.getId() == null || parent.getId().toString().startsWith("#/")) && path.startsWith("#/");
  }

  @Override
  public synchronized void clearCache() {
    this.schemas.clear();
  }

  @Override
  public Integer getSize() {
    return schemas.size();
  }

  @Override
  public Optional<Schema> getById(URI id) {
    for ( Schema schema : schemas.values() ) {
      if ( schema.getId() != null && schema.getId().equals(id) ) {
        return Optional.of(schema);
      }
    }
    return Optional.empty();
  }

  @Override
  public Optional<Schema> getByUri(URI uri) {
    for ( Schema schema : schemas.values() ) {
      if ( schema.getUri().equals(uri) ) {
        return Optional.of(schema);
      }
    }
    return Optional.empty();
  }

  @Override
  public Integer getFileUriCount() {
    int count = 0;
    for ( Schema schema : schemas.values() ) {
      if ( schema.getUri().getScheme().equals("file") ) {
        count++;
      }
    }
    return count;
  }

  @Override
  public Integer getHttpUriCount() {
    int count = 0;
    for ( Schema schema : schemas.values() ) {
      if ( schema.getUri().getScheme().equals("http") ) {
        count++;
      }
    }
    return count;
  }

  @Override
  public Iterator<Schema> getSchemaIterator() {
    List<Schema> schemaList = new ArrayList<>(schemas.values());
    schemaList.sort(this);
    return schemaList.iterator();
  }

  @Override
  public ObjectNode resolveProperties(Schema schema, ObjectNode fieldNode, String resourceId) {
    // this should return something more suitable like:
    //   Map<String, Pair<Schema, ObjectNode>>
    ObjectNode schemaProperties = NODE_FACTORY.objectNode();
    ObjectNode parentProperties = NODE_FACTORY.objectNode();
    if (fieldNode == null) {
      ObjectNode schemaContent = (ObjectNode) schema.getContent();
      if (schemaContent.has("properties")) {
        schemaProperties = (ObjectNode) schemaContent.get("properties");
        if (schema.getParentContent() != null) {
          ObjectNode parentContent = (ObjectNode) schema.getParentContent();
          if (parentContent.has("properties")) {
            parentProperties = (ObjectNode) parentContent.get("properties");
          }
        }
      }
    } else if (fieldNode.size() > 0) {
      if (fieldNode.has("properties") && fieldNode.get("properties").isObject() && fieldNode.get("properties").size() > 0) {
        schemaProperties = (ObjectNode) fieldNode.get("properties");
      }
      URI parentUri = null;
      if ( fieldNode.has("$ref") || fieldNode.has("extends") ) {
        JsonNode refNode = fieldNode.get("$ref");
        JsonNode extendsNode = fieldNode.get("extends");
        if (refNode != null && refNode.isValueNode()) {
          parentUri = URI.create(refNode.asText());
        } else if (extendsNode != null && extendsNode.isObject()) {
          parentUri = URI.create(extendsNode.get("$ref").asText());
        }
        ObjectNode parentContent = null;
        URI absoluteUri;
        if (parentUri.isAbsolute()) {
          absoluteUri = parentUri;
        } else {
          absoluteUri = schema.getUri().resolve(parentUri);
          if (!absoluteUri.isAbsolute() || (absoluteUri.isAbsolute() && !getByUri(absoluteUri).isPresent() )) {
            absoluteUri = schema.getParentUri().resolve(parentUri);
          }
        }
        if (absoluteUri.isAbsolute()) {
          if (getByUri(absoluteUri).isPresent()) {
            parentContent = (ObjectNode) getByUri(absoluteUri).get().getContent();
          }
          if (parentContent != null && parentContent.isObject() && parentContent.has("properties")) {
            parentProperties = (ObjectNode) parentContent.get("properties");
          } else if (absoluteUri.getPath().endsWith("#properties")) {
            absoluteUri = URI.create(absoluteUri.toString().replace("#properties", ""));
            parentProperties = (ObjectNode) getByUri(absoluteUri).get().getContent().get("properties");
          }
        }
      }


    }

    ObjectNode resolvedProperties = NODE_FACTORY.objectNode();
    if (parentProperties != null && parentProperties.size() > 0) {
      resolvedProperties = SchemaUtil.mergeProperties(schemaProperties, parentProperties);
    } else {
      resolvedProperties = schemaProperties.deepCopy();
    }

    return resolvedProperties;
  }

  /**
   * resolve full definition of 'items'.
   * @param schema Schema
   * @param fieldNode ObjectNode
   * @param resourceId resourceId
   * @return ObjectNode
   */
  public ObjectNode resolveItems(Schema schema, ObjectNode fieldNode, String resourceId) {
    ObjectNode schemaItems = NODE_FACTORY.objectNode();
    ObjectNode parentItems = NODE_FACTORY.objectNode();
    if (fieldNode == null) {
      ObjectNode schemaContent = (ObjectNode) schema.getContent();
      if ( schemaContent.has("items") ) {
        schemaItems = (ObjectNode) schemaContent.get("items");
        if (schema.getParentContent() != null) {
          ObjectNode parentContent = (ObjectNode) schema.getParentContent();
          if (parentContent.has("items")) {
            parentItems = (ObjectNode) parentContent.get("items");
          }
        }
      }
    } else if (fieldNode.size() > 0) {
      if (fieldNode.has("items") && fieldNode.get("items").isObject() && fieldNode.get("items").size() > 0) {
        schemaItems = (ObjectNode) fieldNode.get("items");
      }
      URI parentUri = null;
      if ( fieldNode.has("$ref") || fieldNode.has("extends") ) {
        JsonNode refNode = fieldNode.get("$ref");
        JsonNode extendsNode = fieldNode.get("extends");
        if (refNode != null && refNode.isValueNode()) {
          parentUri = URI.create(refNode.asText());
        } else if (extendsNode != null && extendsNode.isObject()) {
          parentUri = URI.create(extendsNode.get("$ref").asText());
        }
        ObjectNode parentContent = null;
        URI absoluteUri;
        if (parentUri.isAbsolute()) {
          absoluteUri = parentUri;
        } else {
          absoluteUri = schema.getUri().resolve(parentUri);
          if (!absoluteUri.isAbsolute() || (absoluteUri.isAbsolute() && !getByUri(absoluteUri).isPresent() )) {
            absoluteUri = schema.getParentUri().resolve(parentUri);
          }
        }
        if (absoluteUri.isAbsolute()) {
          if (getByUri(absoluteUri).isPresent()) {
            parentContent = (ObjectNode) getByUri(absoluteUri).get().getContent();
          }
          if (parentContent != null && parentContent.isObject() && parentContent.has("items")) {
            parentItems = (ObjectNode) parentContent.get("items");
          } else if (absoluteUri.getPath().endsWith("#items")) {
            absoluteUri = URI.create(absoluteUri.toString().replace("#items", ""));
            parentItems = (ObjectNode) getByUri(absoluteUri).get().getContent().get("items");
          }
        }
      }
    }

    ObjectNode resolvedItems = NODE_FACTORY.objectNode();
    if (parentItems != null && parentItems.size() > 0) {
      resolvedItems = SchemaUtil.mergeProperties(schemaItems, parentItems);
    } else {
      resolvedItems = schemaItems.deepCopy();
    }

    return resolvedItems;
  }

  @Override
  public int compare(Schema left, Schema right) {
    // are they the same?
    if ( left.equals(right)) {
      return 0;
    }
    // is one an ancestor of the other
    Schema candidateAncestor = left;
    while ( candidateAncestor.getParent() != null ) {
      candidateAncestor = candidateAncestor.getParent();
      if ( candidateAncestor.equals(right)) {
        return 1;
      }
    }
    candidateAncestor = right;
    while ( candidateAncestor.getParent() != null ) {
      candidateAncestor = candidateAncestor.getParent();
      if ( candidateAncestor.equals(left)) {
        return -1;
      }
    }
    // does one have a field that reference the other?
    for ( JsonNode refNode : left.getContent().findValues("$ref") ) {
      String refText = refNode.asText();
      Optional<URI> resolvedUri = safeResolve(left.getUri(), refText);
      if ( resolvedUri.isPresent() && resolvedUri.get().equals(right.getUri())) {
        return 1;
      }
    }
    for ( JsonNode refNode : right.getContent().findValues("$ref") ) {
      String refText = refNode.asText();
      Optional<URI> resolvedUri = safeResolve(right.getUri(), refText);
      if ( resolvedUri.isPresent() && resolvedUri.get().equals(left.getUri())) {
        return -1;
      }
    }
    // does one have a field that reference a third schema that references the other?
    for ( JsonNode refNode : left.getContent().findValues("$ref") ) {
      String refText = refNode.asText();
      Optional<URI> possibleConnectorUri = safeResolve(left.getUri(), refText);
      if ( possibleConnectorUri.isPresent()) {
        Optional<Schema> possibleConnector = getByUri(possibleConnectorUri.get());
        if (possibleConnector.isPresent()) {
          for (JsonNode connectorRefNode : possibleConnector.get().getContent().findValues("$ref")) {
            String connectorRefText = connectorRefNode.asText();
            Optional<URI> resolvedUri = safeResolve(possibleConnector.get().getUri(), connectorRefText);
            if (resolvedUri.isPresent() && resolvedUri.get().equals(right.getUri())) {
              return 1;
            }
          }
        }
      }
    }
    for ( JsonNode refNode : right.getContent().findValues("$ref") ) {
      String refText = refNode.asText();
      Optional<URI> possibleConnectorUri = safeResolve(right.getUri(), refText);
      if ( possibleConnectorUri.isPresent()) {
        Optional<Schema> possibleConnector = getByUri(possibleConnectorUri.get());
        if (possibleConnector.isPresent()) {
          for (JsonNode connectorRefNode : possibleConnector.get().getContent().findValues("$ref")) {
            String connectorRefText = connectorRefNode.asText();
            Optional<URI> resolvedUri = safeResolve(possibleConnector.get().getUri(), connectorRefText);
            if (resolvedUri.isPresent() && resolvedUri.get().equals(left.getUri())) {
              return -1;
            }
          }
        }
      }
    }
    // there still has to be some order even when there are no connections.
    // we'll arbitrarily pick alphabetic by ID
    int lexigraphic = right.toString().compareTo(left.toString());
    return ( lexigraphic / Math.abs(lexigraphic) );
  }

}

