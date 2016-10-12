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
import java.util.List;

@Mojo(  name = "generate-resources",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES
)
@Execute(   goal = "generate-resources",
            phase = LifecyclePhase.GENERATE_RESOURCES
)
public class StreamsHbaseResourceGeneratorMojo extends AbstractMojo {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHbaseResourceGeneratorMojo.class);

    private volatile MojoFailureException mojoFailureException;

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

    @Parameter( defaultValue = "src/main/jsonschema", readonly = true ) // Maven 3 only
    public String sourceDirectory;

    @Parameter( readonly = true ) // Maven 3 only
    public List<String> sourcePaths;

    @Parameter(defaultValue = "./target/generated-resources/hbase", readonly = true)
    public String targetDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {

        //addProjectDependenciesToClasspath();

        StreamsHbaseGenerationConfig config = new StreamsHbaseGenerationConfig();

        if( sourcePaths != null && sourcePaths.size() > 0)
            config.setSourcePaths(sourcePaths);
        else
            config.setSourceDirectory(sourceDirectory);
        config.setTargetDirectory(targetDirectory);

        StreamsHbaseResourceGenerator streamsHbaseResourceGenerator = new StreamsHbaseResourceGenerator(config);
        streamsHbaseResourceGenerator.run();

    }

}