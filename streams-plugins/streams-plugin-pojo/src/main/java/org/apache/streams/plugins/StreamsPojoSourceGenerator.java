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

import org.apache.maven.plugin.MojoExecutionException;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.RuleLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Embed within your own java code
 *
 * <p></p>
 * StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();
 * config.setSourceDirectory("src/main/jsonschema");
 * config.setTargetDirectory("target/generated-sources/pojo");
 * StreamsPojoSourceGenerator generator = new StreamsPojoSourceGenerator(config);
 * generator.run();
 *
 */
public class StreamsPojoSourceGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGenerator.class);

  private static final String LS = System.getProperty("line.separator");

  private StreamsPojoGenerationConfig config;
  private RuleLogger ruleLogger;

  /**
   * Run from CLI without Maven
   *
   * <p></p>
   * java -jar streams-plugin-pojo-jar-with-dependencies.jar StreamsPojoSourceGenerator src/main/jsonschema target/generated-sources
   *
   * @param args [sourceDirectory, targetDirectory, targetPackage]
   * */
  public static void main(String[] args) throws Exception {
    StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

    String sourceDirectory = "src/main/jsonschema";
    String targetDirectory = "target/generated-sources/pojo";
    String targetPackage = "";

    if ( args.length > 0 ) {
      sourceDirectory = args[0];
    }
    if ( args.length > 1 ) {
      targetDirectory = args[1];
    }
    if ( args.length > 2 ) {
      targetPackage = args[2];
    }

    config.setSourceDirectory(sourceDirectory);
    config.setTargetPackage(targetPackage);
    config.setTargetDirectory(targetDirectory);

    StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
    streamsPojoSourceGenerator.execute();
  }

  public StreamsPojoSourceGenerator(StreamsPojoGenerationConfig config) {
    this.config = config;
  }

  public void execute() throws MojoExecutionException {

    Objects.requireNonNull(config);
    GenerationConfig generationConfig = (GenerationConfig) config;

    ruleLogger = new StreamsPojoRuleLogger();

    try {
      Jsonschema2Pojo.generate(generationConfig, ruleLogger);
    } catch (IOException ex) {
      LOGGER.error("{} {}", ex.getClass(), ex.getMessage());
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  private void writeFile(String pojoFile, String pojoHive) {
    try {
      File path = new File(pojoFile);
      File dir = path.getParentFile();
      if ( !dir.exists() ) {
        dir.mkdirs();
      }
      Files.write(Paths.get(pojoFile), pojoHive.getBytes(), StandardOpenOption.CREATE_NEW);
    } catch (Exception ex) {
      LOGGER.error("Write Exception: {}", ex);
    }
  }
}
