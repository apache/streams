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

package org.apache.streams.juneau;

import org.apache.commons.lang3.StringUtils;
import org.apache.juneau.httppart.HttpPartType;
import org.apache.juneau.httppart.HttpPartSerializer;

import java.util.List;

/**
 * Serializes {@link java.util.List} as appropriately delimited {@link String Strings}.
 */
public class ListSerializer implements HttpPartSerializer {

  @Override
  public String serialize(HttpPartType type, Object value) {
    List list = (List) value;
    if( list.size() > 0 ) {
      if( type.equals(HttpPartType.QUERY)) {
        return StringUtils.join(list, ",");
      } else if( type.equals(HttpPartType.PATH)) {
        return StringUtils.join(list, "/");
      }
    }
    return null;
  }
}
