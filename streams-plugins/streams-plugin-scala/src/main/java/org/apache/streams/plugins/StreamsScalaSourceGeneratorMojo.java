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

import com.google.common.base.Splitter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

@Mojo(
    name = "scala",
    defaultPhase = LifecyclePhase.GENERATE_SOURCES
    )
@Execute(
    goal = "scala",
    phase = LifecyclePhase.GENERATE_SOURCES
    )
public class StreamsScalaSourceGeneratorMojo extends AbstractMojo {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGeneratorMojo.class);

  @Component
  private MavenProject project;

  //    @Component
  //    private Settings settings;
  //
  //    @Parameter( defaultValue = "${localRepository}", readonly = true, required = true )
  //    protected ArtifactRepository localRepository;
  //
  //    @Parameter( defaultValue = "${plugin}", readonly = true ) // Maven 3 only
  //    private PluginDescriptor plugin;
  //
  @Parameter( defaultValue = "${project.basedir}", readonly = true )
  private File basedir;

  @Parameter(defaultValue = "${project.build.directory}", readonly = true)
  private File target;

  @Parameter(defaultValue = "org.apache.streams.pojo.json", readonly = true)
  private String packages;

  /**
   * execute StreamsScalaSourceGeneratorMojo.
   * @throws MojoExecutionException MojoExecutionException
   * @throws MojoFailureException MojoFailureException
   */
  public void execute() throws MojoExecutionException {
    StreamsScalaGenerationConfig config = new StreamsScalaGenerationConfig();
    config.setSourcePackages(Splitter.on(',').splitToList(packages));
    config.setTargetDirectory(target.toString());

    StreamsScalaSourceGenerator streamsScalaSourceGenerator = new StreamsScalaSourceGenerator(config);

    streamsScalaSourceGenerator.run();
  }

  public File getTarget() {
    return target;
  }

  public String getPackages() {
    return packages;
  }
}