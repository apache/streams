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

import java.net.URI;

/**
 * A JSON Schema document.
 */
public class Schema {

  private final URI id;
  private final URI uri;
  private final JsonNode content;
  private final Schema parent;
  private final boolean generate;

  /**
   * Schema constructor.
   * @param uri uri
   * @param content JsonNode content
   * @param parent Schema parent
   * @param generate whether to generate
   */
  public Schema(URI uri, JsonNode content, Schema parent, boolean generate) {
    this.uri = uri;
    this.content = content;
    this.parent = parent;
    this.generate = generate;
    this.id = content.has("id") ? URI.create(content.get("id").asText()) : null;
  }

  public URI getId() {
    return id;
  }

  public URI getUri() {
    return uri;
  }

  public JsonNode getContent() {
    return content;
  }

  /**
   * getParentContent.
   * @return Parent.Content
   */
  public JsonNode getParentContent() {
    if ( parent != null ) {
      return parent.getContent();
    } else {
      return null;
    }
  }

  /**
   * getParentUri.
   * @return Parent.Uri
   */
  public URI getParentUri() {
    if ( parent != null ) {
      return parent.getUri();
    } else {
      return null;
    }
  }

  public boolean isGenerated() {
    return generate;
  }

  public Schema getParent() {
    return parent;
  }

}