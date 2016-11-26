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

/**
 * FieldUtil contains methods to assist in understanding fields defined within schemas.
 */
public class FieldUtil {

  /**
   * determine FieldType from ObjectNode.
   * @param fieldNode ObjectNode
   * @return FieldType
   */
  public static FieldType determineFieldType(ObjectNode fieldNode) {
    String typeSchemaField = "type";
    if ( !fieldNode.has(typeSchemaField)) {
      return null;
    }
    String typeSchemaFieldValue = fieldNode.get(typeSchemaField).asText();
    switch (typeSchemaFieldValue) {
      case "string":
        return FieldType.STRING;
      case "integer":
        return FieldType.INTEGER;
      case "number":
        return FieldType.NUMBER;
      case "object":
        return FieldType.OBJECT;
      case "boolean":
        return FieldType.BOOLEAN;
      case "array":
        return FieldType.ARRAY;
      default:
        return null;
    }
  }

}
