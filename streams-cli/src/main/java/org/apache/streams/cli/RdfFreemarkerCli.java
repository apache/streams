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

package org.apache.streams.cli;

import fmpp.Engine;
import fmpp.TemplateEnvironment;
import fmpp.localdatabuilders.MapLocalDataBuilder;
import fmpp.progresslisteners.ConsoleProgressListener;
import fmpp.progresslisteners.TerseConsoleProgressListener;
import fmpp.setting.Settings;
import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.typesafe.config.Config;

import com.typesafe.config.ConfigFactory;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import org.apache.streams.config.StreamsConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fmpp.setting.Settings.DEFAULT_CFG_FILE_NAME;
import static fmpp.setting.Settings.NAME_DATA;
import static fmpp.setting.Settings.NAME_DATA_ROOT;
import static fmpp.setting.Settings.NAME_LOCAL_DATA;
import static fmpp.setting.Settings.NAME_OUTPUT_ROOT;
import static fmpp.setting.Settings.NAME_SOURCE_ROOT;

public class RdfFreemarkerCli implements Callable {

  private final static Logger LOGGER = LoggerFactory.getLogger(RdfFreemarkerCli.class);
  private String[] args;
  private Config typesafe;

  public static void main(String[] args) throws Exception {
    RdfFreemarkerCli cli = new RdfFreemarkerCli(args);
    try {
      cli.call();
    } catch( Exception e ) {
      LOGGER.error("Error", e);
      System.exit(1);
    }
    System.exit(0);
  }

  public RdfFreemarkerCli(String[] args) {
    ConfigFactory.invalidateCaches();
    this.args = args;
    this.typesafe = StreamsConfigurator.getConfig();
  }

  public Boolean call() throws Exception {

    String baseDir;
    if (typesafe.hasPath("baseDir"))
      baseDir = typesafe.getString("baseDir");
    else
      baseDir = args[0];

    LOGGER.info("baseDir: " + baseDir);
    Path baseDirPath = Paths.get(baseDir);
    assert( Files.exists(baseDirPath) );
    assert( Files.isDirectory(baseDirPath) );

    String settingsFile;
    if (typesafe.hasPath("settingsFile"))
      settingsFile = typesafe.getString("settingsFile");
    else
      settingsFile = args[1];

    LOGGER.info("settingsFile: " + settingsFile);
    Path settingsFilePath = Paths.get(settingsFile);
    assert( Files.exists(settingsFilePath) );
    assert( !Files.isDirectory(settingsFilePath) );

    String sourceRoot;
    if (typesafe.hasPath("sourceRoot"))
      sourceRoot = typesafe.getString("sourceRoot");
    else
      sourceRoot = args[2];

    LOGGER.info("sourceRoot: " + sourceRoot);
    Path sourceRootPath = Paths.get(sourceRoot);
    assert( Files.exists(sourceRootPath) );
    assert( Files.isDirectory(sourceRootPath) );

    String dataRoot;
    if (typesafe.hasPath("dataRoot"))
      dataRoot = typesafe.getString("dataRoot");
    else
      dataRoot = args[3];

    LOGGER.info("dataRoot: " + dataRoot);
    Path dataRootPath = Paths.get(dataRoot);
    assert( Files.exists(dataRootPath) );
    assert( Files.isDirectory(dataRootPath) );

    String outputRoot;
    if (typesafe.hasPath("outputRoot"))
      outputRoot = typesafe.getString("outputRoot");
    else
      outputRoot = args[4];

    LOGGER.info("outputRoot: " + outputRoot);
    Path outputRootPath = Paths.get(outputRoot);
    assert( Files.exists(outputRootPath) );
    assert( Files.isDirectory(outputRootPath) );

    String namespace;
    if (typesafe.hasPath("namespace"))
      namespace = typesafe.getString("namespace");
    else
      namespace = args[5];

    String id;
    if (typesafe.hasPath("id"))
      id = typesafe.getString("id");
    else
      id = args[6];

    Settings settings = new Settings(baseDirPath.toFile());
    settings.load(settingsFilePath.toFile());
    settings.set(NAME_DATA_ROOT, dataRoot);
    settings.set(NAME_SOURCE_ROOT, sourceRoot);
    settings.set(NAME_OUTPUT_ROOT, outputRoot);

    Map<String, String> vars = new HashMap<>();
    vars.put("dataRoot", dataRoot);
    vars.put("id", id);
    vars.put("namespace", namespace);

    settings.set(NAME_DATA, vars);

//    settings.define("id", Settings.TYPE_STRING, true, true);
//    settings.set("id", id);
//    settings.define("namespace", Settings.TYPE_STRING, true, true);
//    settings.set("namespace", namespace);

    ConsoleProgressListener listener = new ConsoleProgressListener();
    settings.addProgressListener(listener);

    try {
      settings.execute();
    } catch( Exception ex ) {
      LOGGER.error("settings.execute() Exception", ex);
      ex.printStackTrace();
      return false;
    }
    LOGGER.info("settings.execute() Success");
    return true;
  }

  public String dropExtension(String path) {
    return path.substring(0, path.lastIndexOf('.'));
  }

}