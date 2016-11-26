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

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.util.Optional;

/**
 * UriUtil contains methods to assist in resolving URIs and URI fragments.
 */
public class UriUtil {

  public static URI removeFragment(URI id) {
    return URI.create(StringUtils.substringBefore(id.toString(), "#"));
  }

  public static URI removeFile(URI id) {
    return URI.create(StringUtils.substringBeforeLast(id.toString(), "/"));
  }

  /**
   * resolve a remote schema safely.
   * @param absolute root URI
   * @param relativePart relative to root
   * @return URI if resolvable, or Optional.absent()
   */
  public static Optional<URI> safeResolve(URI absolute, String relativePart) {
    if ( !absolute.isAbsolute()) {
      return Optional.empty();
    }
    try {
      return Optional.of(absolute.resolve(relativePart));
    } catch ( IllegalArgumentException ex ) {
      return Optional.empty();
    }
  }

}
