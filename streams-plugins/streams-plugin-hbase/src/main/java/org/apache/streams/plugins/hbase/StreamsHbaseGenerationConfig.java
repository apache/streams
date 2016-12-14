/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.plugins.hbase;

import org.apache.streams.util.schema.GenerationConfig;

import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.util.URLUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Configures StreamsHiveResourceGenerator.
 */
public class StreamsHbaseGenerationConfig extends DefaultGenerationConfig implements GenerationConfig {

  public String getSourceDirectory() {
    return sourceDirectory;
  }

  public List<String> getSourcePaths() {
    return sourcePaths;
  }

  private String columnFamily;
  private String sourceDirectory;
  private List<String> sourcePaths = new ArrayList<>();
  private String targetDirectory;
  private int maxDepth = 1;

  public Set<String> getExclusions() {
    return exclusions;
  }

  public void setExclusions(Set<String> exclusions) {
    this.exclusions = exclusions;
  }

  private Set<String> exclusions = new HashSet<>();

  public int getMaxDepth() {
    return maxDepth;
  }

  public void setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public void setSourcePaths(List<String> sourcePaths) {
    this.sourcePaths = sourcePaths;
  }

  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public File getTargetDirectory() {
    return new File(targetDirectory);
  }

  /**
   * get all sources.
   * @return Iterator of URL
   */
  public Iterator<URL> getSource() {
    if (null != sourceDirectory) {
      return Collections.singleton(URLUtil.parseURL(sourceDirectory)).iterator();
    }
    List<URL> sourceUrls = new ArrayList<>();
    if ( sourcePaths != null && sourcePaths.size() > 0) {
      for (String source : sourcePaths) {
        sourceUrls.add(URLUtil.parseURL(source));
      }
    }
    return sourceUrls.iterator();
  }

  public void setMaxDepth(int maxDepth) {
    this.maxDepth = maxDepth;
  }

  public String getColumnFamily() {
    return columnFamily;
  }

  public void setColumnFamily(String columnFamily) {
    this.columnFamily = columnFamily;
  }
}
