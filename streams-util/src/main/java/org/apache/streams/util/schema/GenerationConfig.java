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

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.Iterator;

/**
 * GenerationConfig represents the common fields and field accessors for
 * streams modules that transform schemas into generated-sources or generated-resources.
 */
public interface GenerationConfig {

  /**
   * Gets the 'source' configuration option.
   *
   * @return The source file(s) or directory(ies) from which JSON Schema will
   *         be read.
   */
  Iterator<URL> getSource();

  /**
   * Gets the 'targetDirectory' configuration option.
   *
   * @return The target directory into which generated types will be written
   *         (may or may not exist before types are written)
   */
  File getTargetDirectory();

  /**
   * Gets the 'outputEncoding' configuration option.
   *
   * @return The character encoding that should be used when writing output files.
   */
  String getOutputEncoding();

  /**
   * Gets the file filter used to isolate the schema mapping files in the
   * source directories.
   *
   * @return the file filter use when scanning for schema files.
   */
  FileFilter getFileFilter();

}
