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

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.maven.ProjectClasspath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

@Mojo (
    name = "generate-sources",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
    )
@Execute (
    goal = "generate-sources",
    phase = LifecyclePhase.GENERATE_SOURCES
    )
public class StreamsPojoSourceGeneratorMojo extends AbstractMojo {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorMojo.class);

  private volatile MojoFailureException mojoFailureException;

  @Component
  public MavenProject project;

  @Parameter( defaultValue = "${project.basedir}", readonly = true )
  public File basedir;

  @Parameter( defaultValue = "./src/main/jsonschema", readonly = true ) // Maven 3 only
  public String sourceDirectory;

  @Parameter( readonly = true ) // Maven 3 only
  public List<String> sourcePaths;

  @Parameter(defaultValue = "./target/generated-sources/pojo", readonly = true)
  public String targetDirectory;

  @Parameter(readonly = true)
  public String targetPackage;

  /**
   * execute StreamsPojoSourceGenerator.
   * @throws MojoExecutionException MojoExecutionException
   * @throws MojoFailureException MojoFailureException
   */
  public void execute() throws MojoExecutionException, MojoFailureException {

    addProjectDependenciesToClasspath();

    StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

    if ( sourcePaths != null && sourcePaths.size() > 0) {
      config.setSourcePaths(sourcePaths);
    } else {
      config.setSourceDirectory(sourceDirectory);
    }
    config.setTargetPackage(targetPackage);
    config.setTargetDirectory(targetDirectory);

    StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);

    streamsPojoSourceGenerator.execute();

  }

  private void addProjectDependenciesToClasspath() {

    try {

      ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
      ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
      Thread.currentThread().setContextClassLoader(newClassLoader);

    } catch (DependencyResolutionRequiredException ex) {
      LOGGER.info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", ex);
    }

  }

}