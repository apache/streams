package org.apache.streams.plugins;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jsonschema2pojo.Jsonschema2Pojo;
import org.jsonschema2pojo.maven.Jsonschema2PojoMojo;
import org.jsonschema2pojo.maven.ProjectClasspath;
import org.jsonschema2pojo.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Mojo(  name = "pojo",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES
)
@Execute(   goal = "pojo",
            phase = LifecyclePhase.GENERATE_SOURCES
)
public class StreamsPojoSourceGeneratorMojo extends AbstractMojo {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorMojo.class);

    @Component
    public MavenProject project;

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
    public File basedir;

    @Parameter( defaultValue = "./src/main/jsonschema", readonly = true ) // Maven 3 only
    public String sourceDirectory;

    @Parameter( readonly = true ) // Maven 3 only
    public List<String> sourcePaths;

    @Parameter(defaultValue = "./target/generated-sources/streams-plugin-pojo", readonly = true)
    public String targetDirectory;

    @Parameter(readonly = true)
    public String targetPackage;

    public void execute() throws MojoExecutionException {

        addProjectDependenciesToClasspath();

        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        if( sourcePaths != null && sourcePaths.size() > 0)
            config.setSourcePaths(sourcePaths);
        else
            config.setSourceDirectory(sourceDirectory);
        config.setTargetPackage(targetPackage);
        config.setTargetDirectory(targetDirectory);

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        Thread thread = new Thread(streamsPojoSourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return;

//        List<Class<?>> serializableClasses = detectSerializableClasses();
//
//        LOGGER.info("Detected {} serialiables:", serializableClasses.size());
//        for( Class clazz : serializableClasses )
//            LOGGER.debug(clazz.toString());
//
//        List<Class<?>> pojoClasses = detectPojoClasses(serializableClasses);
//
//        LOGGER.info("Detected {} pojos:", pojoClasses.size());
//        for( Class clazz : pojoClasses ) {
//            LOGGER.debug(clazz.toString());
//
//        }
//
//
//        for( Class clazz : pojoClasses ) {
//            String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".hive").replace(".","/")+"/";
//            String pojoName = clazz.getSimpleName()+".hql";
//            String pojoHive = renderPojo(clazz);
//            writeFile(outDir+"/"+pojoPath+pojoName, pojoHive);
//        }

    }

    private void addProjectDependenciesToClasspath() {

        try {

            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newClassLoader = new ProjectClasspath().getClassLoader(project, oldClassLoader, getLog());
            Thread.currentThread().setContextClassLoader(newClassLoader);

        } catch (DependencyResolutionRequiredException e) {
            LOGGER.info("Skipping addition of project artifacts, there appears to be a dependecy resolution problem", e);
        }

    }

//    public List<Class<?>> detectSerializableClasses() {
//
//        Set<Class<? extends Serializable>> classes =
//                reflections.getSubTypesOf(java.io.Serializable.class);
//
//        List<Class<?>> result = Lists.newArrayList();
//
//        for( Class clazz : classes ) {
//            result.add(clazz);
//        }
//
//        return result;
//    }
//
//    public List<Class<?>> detectPojoClasses(List<Class<?>> classes) {
//
//        List<Class<?>> result = Lists.newArrayList();
//
//        for( Class clazz : classes ) {
//            try {
//                clazz.newInstance().toString();
//            } catch( Exception e) {}
//            // super-halfass way to know if this is a jsonschema2pojo
//            if( clazz.getAnnotations().length >= 1 )
//                result.add(clazz);
//        }
//
//        return result;
//    }
//
//    public String renderPojo(Class<?> pojoClass) {
//        StringBuffer stringBuffer = new StringBuffer();
//        stringBuffer.append("CREATE TABLE ");
//        stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".hive"));
//        stringBuffer.append(LS);
//        stringBuffer.append("(");
//        stringBuffer.append(LS);
//
//        Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
//        appendFields(stringBuffer, fields, "", ",");
//
//        stringBuffer.append(")");
//
//        return stringBuffer.toString();
//    }
//
//    private void appendFields(StringBuffer stringBuffer, Set<Field> fields, String varDef, String fieldDelimiter) {
//        if( fields.size() > 0 ) {
//            stringBuffer.append(LS);
//            Map<String,Field> fieldsToAppend = uniqueFields(fields);
//            for( Iterator<Field> iter = fieldsToAppend.values().iterator(); iter.hasNext(); ) {
//                Field field = iter.next();
//                stringBuffer.append(name(field));
//                stringBuffer.append(": ");
//                stringBuffer.append(type(field));
//                if( iter.hasNext()) stringBuffer.append(fieldDelimiter);
//                stringBuffer.append(LS);
//            }
//        } else {
//            stringBuffer.append(LS);
//        }
//    }
//
//    private String value(Field field) {
//        if( field.getName().equals("verb")) {
//            return "\"post\"";
//        } else if( field.getName().equals("objectType")) {
//            return "\"application\"";
//        } else return null;
//    }
//
//    private String type(Field field) {
//        if( field.getType().equals(java.lang.String.class)) {
//            return "STRING";
//        } else if( field.getType().equals(java.lang.Integer.class)) {
//            return "INT";
//        } else if( field.getType().equals(org.joda.time.DateTime.class)) {
//            return "DATE";
//        }else if( field.getType().equals(java.util.Map.class)) {
//            return "MAP";
//        } else if( field.getType().equals(java.util.List.class)) {
//            return "ARRAY";
//        }
//        return field.getType().getCanonicalName().replace(".pojo.json", ".scala");
//    }
//
//    private Map<String,Field> uniqueFields(Set<Field> fieldset) {
//        Map<String,Field> fields = Maps.newTreeMap();
//        Field item = null;
//        for( Iterator<Field> it = fieldset.iterator(); it.hasNext(); item = it.next() ) {
//            if( item != null && item.getName() != null ) {
//                Field added = fields.put(item.getName(), item);
//            }
//            // ensure right class will get used
//        }
//        return fields;
//    }
//
//    private String name(Field field) {
//        if( field.getName().equals("object"))
//            return "obj";
//        else return field.getName();
//    }
//
//    private boolean override(Field field) {
//        try {
//            if( field.getDeclaringClass().getSuperclass().getField(field.getName()) != null )
//                return true;
//            else return false;
//        } catch( Exception e ) {
//            return false;
//        }
//    }
}