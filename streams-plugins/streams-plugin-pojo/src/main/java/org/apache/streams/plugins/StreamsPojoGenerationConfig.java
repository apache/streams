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

package org.apache.streams.plugins;

import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.jsonschema2pojo.util.URLUtil;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Configures StreamsPojoSourceGenerator.
 */
public class StreamsPojoGenerationConfig extends DefaultGenerationConfig {

  private String sourceDirectory;
  private List<String> sourcePaths;
  private String targetPackage;
  private String targetDirectory;

  public void setSourceDirectory(String sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public void setSourcePaths(List<String> sourcePaths) {
    this.sourcePaths = sourcePaths;
  }

  public void setTargetPackage(String targetPackage) {
    this.targetPackage = targetPackage;
  }

  public void setTargetDirectory(String targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  @Override
  public String getTargetPackage() {
    return targetPackage;
  }

  @Override
  public File getTargetDirectory() {
    return new File(targetDirectory);
  }

  @Override
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

  @Override
  public boolean isGenerateBuilders() {
    return true;
  }

  @Override
  public boolean isUseLongIntegers() {
    return true;
  }

  @Override
  public boolean isRemoveOldOutput() {
    return true;
  }

  @Override
  public boolean isUseJodaDates() {
    return true;
  }

  @Override
  public boolean isIncludeJsr303Annotations() {
    return true;
  }

  @Override
  public boolean isSerializable() {
    return true;
  }

  @Override
  public boolean isIncludeGeneratedAnnotation() {
    return false;
  }

  @Override
  public Class<? extends Annotator> getCustomAnnotator() {
    return JuneauPojoAnnotator.class;
  }

}
