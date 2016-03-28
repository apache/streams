package org.apache.streams.plugins;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.reflections.ReflectionUtils;
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
 * Created by sblackmon on 11/18/15.
 */
public class StreamsHiveResourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsHiveResourceGeneratorMojo mojo;

    String inDir = "./target/test-classes/activities";
    String outDir = "./target/generated-sources/hive";

    public void main(String[] args) {
        StreamsHiveResourceGenerator streamsHiveResourceGenerator = new StreamsHiveResourceGenerator();
        Thread thread = new Thread(streamsHiveResourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }
        return;
    }

    public StreamsHiveResourceGenerator(StreamsHiveResourceGeneratorMojo mojo) {
        this.mojo = mojo;
        if (    mojo != null &&
                mojo.getTarget() != null &&
                !Strings.isNullOrEmpty(mojo.getTarget().getAbsolutePath())
            )
            outDir = mojo.getTarget().getAbsolutePath();

        if (    mojo != null &&
                mojo.getPackages() != null &&
                mojo.getPackages().length > 0
            )
            packages = mojo.getPackages();
    }

    public StreamsHiveResourceGenerator() {
    }

    public void run() {

        List<File> schemaFiles;


        List<Class<?>> serializableClasses = detectSerializableClasses();

        LOGGER.info("Detected {} serialiables:", serializableClasses.size());
        for( Class clazz : serializableClasses )
            LOGGER.debug(clazz.toString());

        List<Class<?>> pojoClasses = detectPojoClasses(serializableClasses);

        LOGGER.info("Detected {} pojos:", pojoClasses.size());
        for( Class clazz : pojoClasses ) {
            LOGGER.debug(clazz.toString());

        }


        for( Class clazz : pojoClasses ) {
            String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".hive").replace(".","/")+"/";
            String pojoName = clazz.getSimpleName()+".hql";
            String pojoHive = renderPojo(clazz);
            writeFile(outDir+"/"+pojoPath+pojoName, pojoHive);
        }

    }

    private void writeFile(String pojoFile, String pojoHive) {
        try {
            File path = new File(pojoFile);
            File dir = path.getParentFile();
            if( !dir.exists() )
                dir.mkdirs();
            Files.write(Paths.get(pojoFile), pojoHive.getBytes(), StandardOpenOption.CREATE_NEW);
        } catch (Exception e) {
            LOGGER.error("Write Exception: {}", e);
        }
    }

    public List<Class<?>> detectSerializableClasses() {

        Set<Class<? extends Serializable>> classes =
                reflections.getSubTypesOf(java.io.Serializable.class);

        List<Class<?>> result = Lists.newArrayList();

        for( Class clazz : classes ) {
            result.add(clazz);
        }

        return result;
    }

    public List<Class<?>> detectPojoClasses(List<Class<?>> classes) {

        List<Class<?>> result = Lists.newArrayList();

        for( Class clazz : classes ) {
            try {
                clazz.newInstance().toString();
            } catch( Exception e) {}
            // super-halfass way to know if this is a jsonschema2pojo
            if( clazz.getAnnotations().length >= 1 )
                result.add(clazz);
        }

        return result;
    }

    public String renderPojo(Class<?> pojoClass) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("CREATE TABLE ");
        stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".hive"));
        stringBuffer.append(LS);
        stringBuffer.append("(");
        stringBuffer.append(LS);

        Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
        appendFields(stringBuffer, fields, "", ",");

        stringBuffer.append(")");

        return stringBuffer.toString();
    }

    private void appendFields(StringBuffer stringBuffer, Set<Field> fields, String varDef, String fieldDelimiter) {
        if( fields.size() > 0 ) {
            stringBuffer.append(LS);
            Map<String,Field> fieldsToAppend = uniqueFields(fields);
            for( Iterator<Field> iter = fieldsToAppend.values().iterator(); iter.hasNext(); ) {
                Field field = iter.next();
                stringBuffer.append(name(field));
                stringBuffer.append(": ");
                stringBuffer.append(type(field));
                if( iter.hasNext()) stringBuffer.append(fieldDelimiter);
                stringBuffer.append(LS);
            }
        } else {
            stringBuffer.append(LS);
        }
    }

    private String value(Field field) {
        if( field.getName().equals("verb")) {
            return "\"post\"";
        } else if( field.getName().equals("objectType")) {
            return "\"application\"";
        } else return null;
    }

    private String type(Field field) {
        if( field.getType().equals(java.lang.String.class)) {
            return "STRING";
        } else if( field.getType().equals(java.lang.Integer.class)) {
            return "INT";
        } else if( field.getType().equals(org.joda.time.DateTime.class)) {
            return "DATE";
        }else if( field.getType().equals(java.util.Map.class)) {
            return "MAP";
        } else if( field.getType().equals(java.util.List.class)) {
            return "ARRAY";
        }
        return field.getType().getCanonicalName().replace(".pojo.json", ".scala");
    }

    private Map<String,Field> uniqueFields(Set<Field> fieldset) {
        Map<String,Field> fields = Maps.newTreeMap();
        Field item = null;
        for( Iterator<Field> it = fieldset.iterator(); it.hasNext(); item = it.next() ) {
            if( item != null && item.getName() != null ) {
                Field added = fields.put(item.getName(), item);
            }
            // ensure right class will get used
        }
        return fields;
    }

    private String name(Field field) {
        if( field.getName().equals("object"))
            return "obj";
        else return field.getName();
    }

    private boolean override(Field field) {
        try {
            if( field.getDeclaringClass().getSuperclass().getField(field.getName()) != null )
                return true;
            else return false;
        } catch( Exception e ) {
            return false;
        }
    }
}
