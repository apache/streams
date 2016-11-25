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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Embed within your own java code
 *
 * <p/>
 * StreamsScalaGenerationConfig config = new StreamsScalaGenerationConfig();
 * config.setTargetDirectory("target/generated-sources/scala");
 * config.setTargetPackage("com.example");
 * StreamsScalaSourceGenerator generator = new StreamsScalaSourceGenerator(config);
 * generator.run();
 *
 */
public class StreamsScalaSourceGenerator implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGenerator.class);

  private static final String LS = System.getProperty("line.separator");

  private StreamsScalaGenerationConfig config;

  private Reflections reflections;

  private String outDir;

  /**
   * Run from CLI without Maven
   *
   * <p/>
   * java -jar streams-plugin-scala-jar-with-dependencies.jar StreamsScalaSourceGenerator target/generated-sources
   *
   * @param args [targetDirectory, targetPackage]
   * */
  public static void main(String[] args) {
    StreamsScalaGenerationConfig config = new StreamsScalaGenerationConfig();

    List<String> sourcePackages = Lists.newArrayList();
    String targetDirectory = "target/generated-sources/pojo";
    String targetPackage = "";

    if ( args.length > 0 ) {
      sourcePackages = Splitter.on(',').splitToList(args[0]);
    }
    if ( args.length > 1 ) {
      targetDirectory = args[1];
    }
    if ( args.length > 2 ) {
      targetPackage = args[2];
    }

    config.setSourcePackages(sourcePackages);
    config.setTargetPackage(targetPackage);
    config.setTargetDirectory(targetDirectory);

    StreamsScalaSourceGenerator streamsScalaSourceGenerator = new StreamsScalaSourceGenerator(config);
    streamsScalaSourceGenerator.run();
  }

  /**
   * StreamsScalaSourceGenerator constructor.
   * @param config StreamsScalaGenerationConfig
   */
  public StreamsScalaSourceGenerator(StreamsScalaGenerationConfig config) {
    this.config = config;
    this.outDir = config.getTargetDirectory().getAbsolutePath();
    reflections = new Reflections(
        new ConfigurationBuilder()
            // TODO
            .forPackages(
                config.getSourcePackages()
                    .toArray(new String[config.getSourcePackages().size()])
            )
            .setScanners(
                new SubTypesScanner(),
                new TypeAnnotationsScanner()));

  }

  @Override
  public void run() {

    List<Class<?>> serializableClasses = detectSerializableClasses();

    LOGGER.info("Detected {} serialiables:", serializableClasses.size());
    for ( Class clazz : serializableClasses ) {
      LOGGER.debug(clazz.toString());
    }

    List<Class<?>> pojoClasses = detectPojoClasses(serializableClasses);

    LOGGER.info("Detected {} pojos:", pojoClasses.size());
    for ( Class clazz : pojoClasses ) {
      LOGGER.debug(clazz.toString());
    }

    List<Class<?>> traits = detectTraits(pojoClasses);

    LOGGER.info("Detected {} traits:", traits.size());
    for ( Class clazz : traits ) {
      LOGGER.debug(clazz.toString());
    }

    List<Class<?>> cases = detectCases(pojoClasses);

    LOGGER.info("Detected {} cases:", cases.size());
    for ( Class clazz : cases ) {
      LOGGER.debug(clazz.toString());
    }

    for ( Class clazz : traits ) {
      String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/") + "/traits/";
      String pojoName = clazz.getSimpleName() + ".scala";
      String pojoScala = renderTrait(clazz);
      writeFile(outDir + "/" + pojoPath + pojoName, pojoScala);
    }

    for ( Class clazz : traits ) {
      String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/") + "/";
      String pojoName = clazz.getSimpleName() + ".scala";
      String pojoScala = renderClass(clazz);
      writeFile(outDir + "/" + pojoPath + pojoName, pojoScala);
    }

    for ( Class clazz : cases ) {
      String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/") + "/";
      String pojoName = clazz.getSimpleName() + ".scala";
      String pojoScala = renderCase(clazz);
      writeFile(outDir + "/" + pojoPath + pojoName, pojoScala);
    }

  }

  private void writeFile(String pojoFile, String pojoScala) {
    try {
      File path = new File(pojoFile);
      File dir = path.getParentFile();
      if ( !dir.exists() ) {
        dir.mkdirs();
      }
      Files.write(Paths.get(pojoFile), pojoScala.getBytes(), StandardOpenOption.CREATE_NEW);
    } catch (Exception ex) {
      LOGGER.error("Write Exception: {}", ex);
    }
  }

  /**
   * detectSerializableClasses.
   * @return List of Serializable Classes
   */
  public List<Class<?>> detectSerializableClasses() {

    Set<Class<? extends Serializable>> classes =
        reflections.getSubTypesOf(java.io.Serializable.class);

    List<Class<?>> result = Lists.newArrayList();

    for ( Class clazz : classes ) {
      result.add(clazz);
    }

    return result;
  }

  /**
   * detect which Classes are Pojo Classes.
   * @param classes List of candidate Pojo Classes
   * @return List of actual Pojo Classes
   */
  public List<Class<?>> detectPojoClasses(List<Class<?>> classes) {

    List<Class<?>> result = Lists.newArrayList();

    for ( Class clazz : classes ) {
      try {
        clazz.newInstance().toString();
      } catch ( Exception ex) {
        //
      }
      // super-halfass way to know if this is a jsonschema2pojo
      if ( clazz.getAnnotations().length >= 1 ) {
        result.add(clazz);
      }
    }

    return result;
  }

  private List<Class<?>> detectTraits(List<Class<?>> classes) {

    List<Class<?>> traits = Lists.newArrayList();

    for ( Class clazz : classes ) {
      if (reflections.getSubTypesOf(clazz).size() > 0) {
        traits.add(clazz);
      }
    }

    return traits;
  }

  private List<Class<?>> detectCases(List<Class<?>> classes) {

    List<Class<?>> cases = Lists.newArrayList();

    for ( Class clazz : classes ) {
      if (reflections.getSubTypesOf(clazz).size() == 0) {
        cases.add(clazz);
      }
    }

    return cases;
  }

  private String renderTrait(Class<?> pojoClass) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("package ");
    stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
    stringBuffer.append(".traits");
    stringBuffer.append(LS);
    stringBuffer.append("trait " + pojoClass.getSimpleName());
    stringBuffer.append(" extends Serializable");
    stringBuffer.append(" {");

    Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
    appendFields(stringBuffer, fields, "def", ";");

    stringBuffer.append("}");

    return stringBuffer.toString();
  }

  private String renderClass(Class<?> pojoClass) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("package ");
    stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
    stringBuffer.append(LS);
    stringBuffer.append("import org.apache.commons.lang.builder.{HashCodeBuilder, EqualsBuilder, ToStringBuilder}");
    stringBuffer.append(LS);
    stringBuffer.append("class " + pojoClass.getSimpleName());
    stringBuffer.append(" (");

    Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
    appendFields(stringBuffer, fields, "var", ",");

    stringBuffer.append(")");
    stringBuffer.append(" extends " + pojoClass.getPackage().getName().replace(".pojo.json", ".scala") + ".traits." + pojoClass.getSimpleName());
    stringBuffer.append(" with Serializable ");
    stringBuffer.append("{ ");
    stringBuffer.append(LS);
    stringBuffer.append("override def equals(obj: Any) = obj match { ");
    stringBuffer.append(LS);
    stringBuffer.append("  case other: ");
    stringBuffer.append(pojoClass.getSimpleName());
    stringBuffer.append(" => other.getClass == getClass && EqualsBuilder.reflectionEquals(this,obj)");
    stringBuffer.append(LS);
    stringBuffer.append("  case _ => false");
    stringBuffer.append(LS);
    stringBuffer.append("}");
    stringBuffer.append(LS);
    stringBuffer.append("override def hashCode = new HashCodeBuilder().hashCode");
    stringBuffer.append(LS);
    stringBuffer.append("}");

    return stringBuffer.toString();
  }

  private String renderCase(Class<?> pojoClass) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("package ");
    stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
    stringBuffer.append(LS);
    stringBuffer.append("case class " + pojoClass.getSimpleName());
    stringBuffer.append("(");
    Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
    appendFields(stringBuffer, fields, "var", ",");
    stringBuffer.append(")");
    if ( pojoClass.getSuperclass() != null && !pojoClass.getSuperclass().equals(java.lang.Object.class)) {
      stringBuffer.append(" extends " + pojoClass.getSuperclass().getPackage().getName().replace(".pojo.json", ".scala") + ".traits." + pojoClass.getSuperclass().getSimpleName());
    }
    stringBuffer.append(LS);

    return stringBuffer.toString();
  }

  private void appendFields(StringBuffer stringBuffer, Set<Field> fields, String varDef, String fieldDelimiter) {
    if ( fields.size() > 0 ) {
      stringBuffer.append(LS);
      Map<String,Field> fieldsToAppend = uniqueFields(fields);
      for ( Iterator<Field> iter = fieldsToAppend.values().iterator(); iter.hasNext(); ) {
        Field field = iter.next();
        if ( override( field ) ) {
          stringBuffer.append("override ");
        }
        stringBuffer.append(varDef);
        stringBuffer.append(" ");
        stringBuffer.append(name(field));
        stringBuffer.append(": ");
        if ( option(field) ) {
          stringBuffer.append("scala.Option[");
          stringBuffer.append(type(field));
          stringBuffer.append("]");
        } else {
          stringBuffer.append(type(field));
        }
        if ( !fieldDelimiter.equals(";") && value(field) != null) {
          stringBuffer.append(" = ");
          if ( option(field) ) {
            stringBuffer.append("scala.Some(");
            stringBuffer.append(value(field));
            stringBuffer.append(")");
          } else {
            stringBuffer.append(value(field));
          }
        }
        if ( iter.hasNext()) {
          stringBuffer.append(fieldDelimiter);
        }
        stringBuffer.append(LS);
      }
    } else {
      stringBuffer.append(LS);
    }
  }

  private boolean option(Field field) {
    if ( field.getName().equals("verb")) {
      return false;
    } else if ( field.getType().equals(java.util.Map.class)) {
      return false;
    } else if ( field.getType().equals(java.util.List.class)) {
      return false;
    } else {
      return true;
    }
  }

  private String value(Field field) {
    if ( field.getName().equals("verb")) {
      return "\"post\"";
    } else if ( field.getName().equals("objectType")) {
      return "\"application\"";
    } else {
      return null;
    }
  }

  private String type(Field field) {
    if ( field.getType().equals(java.lang.String.class)) {
      return "String";
    } else if ( field.getType().equals(java.util.Map.class)) {
      return "scala.collection.mutable.Map[String,Any]";
    } else if ( field.getType().equals(java.util.List.class)) {
      return "scala.collection.mutable.MutableList[Any]";
    }
    return field.getType().getCanonicalName().replace(".pojo.json", ".scala");
  }

  private Map<String,Field> uniqueFields(Set<Field> fieldset) {
    Map<String,Field> fields = Maps.newTreeMap();
    Field item = null;
    for ( Iterator<Field> it = fieldset.iterator(); it.hasNext(); item = it.next() ) {
      if ( item != null && item.getName() != null ) {
        Field added = fields.put(item.getName(), item);
      }
      // ensure right class will get used
    }
    return fields;
  }

  private String name(Field field) {
    if ( field.getName().equals("object")) {
      return "obj";
    } else {
      return field.getName();
    }
  }

  private boolean override(Field field) {
    try {
      if ( field.getDeclaringClass().getSuperclass().getField(field.getName()) != null ) {
        return true;
      } else {
        return false;
      }
    } catch ( Exception ex ) {
      return false;
    }
  }
}
