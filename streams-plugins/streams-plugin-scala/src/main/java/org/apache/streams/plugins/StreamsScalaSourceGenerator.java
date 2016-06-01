package org.apache.streams.plugins;

import com.google.common.base.Strings;
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
 * Created by sblackmon on 11/18/15.
 */
public class StreamsScalaSourceGenerator implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGenerator.class);

    private final static String LS = System.getProperty("line.separator");

    private StreamsScalaSourceGeneratorMojo mojo;

    String outDir = "./target/generated-sources/scala";
    String packages = "org.apache.streams.pojo.json";

    private final Reflections reflections = new Reflections(
            new ConfigurationBuilder()
                    // TODO
                    .forPackages(packages)
                    .setScanners(
                            new SubTypesScanner(),
                            new TypeAnnotationsScanner()));

    public void main(String[] args) {
        StreamsScalaSourceGenerator streamsScalaSourceGenerator = new StreamsScalaSourceGenerator();
        streamsScalaSourceGenerator.run();
    }

    public StreamsScalaSourceGenerator(StreamsScalaSourceGeneratorMojo mojo) {
        this.mojo = mojo;
        if (    mojo != null &&
                mojo.getTarget() != null &&
                !Strings.isNullOrEmpty(mojo.getTarget().getAbsolutePath())
            )
            outDir = mojo.getTarget().getAbsolutePath();

        if (    mojo != null &&
                mojo.getPackages() != null &&
                !Strings.isNullOrEmpty(mojo.getPackages())
            )
            packages = mojo.getPackages();
    }

    public StreamsScalaSourceGenerator() {
    }

    public void run() {

        List<Class<?>> serializableClasses = detectSerializableClasses();

        LOGGER.info("Detected {} serialiables:", serializableClasses.size());
        for( Class clazz : serializableClasses )
            LOGGER.debug(clazz.toString());

        List<Class<?>> pojoClasses = detectPojoClasses(serializableClasses);

        LOGGER.info("Detected {} pojos:", pojoClasses.size());
        for( Class clazz : pojoClasses )
            LOGGER.debug(clazz.toString());

        List<Class<?>> traits = detectTraits(pojoClasses);

        LOGGER.info("Detected {} traits:", traits.size());
        for( Class clazz : traits )
            LOGGER.debug(clazz.toString());

        List<Class<?>> cases = detectCases(pojoClasses);

        LOGGER.info("Detected {} cases:", cases.size());
        for( Class clazz : cases )
            LOGGER.debug(clazz.toString());


        for( Class clazz : traits ) {
            String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/")+"/traits/";
            String pojoName = clazz.getSimpleName()+".scala";
            String pojoScala = renderTrait(clazz);
            writeFile(outDir+"/"+pojoPath+pojoName, pojoScala);
        }

        for( Class clazz : traits ) {
            String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/")+"/";
            String pojoName = clazz.getSimpleName()+".scala";
            String pojoScala = renderClass(clazz);
            writeFile(outDir+"/"+pojoPath+pojoName, pojoScala);
        }

        for( Class clazz : cases ) {
            String pojoPath = clazz.getPackage().getName().replace(".pojo.json", ".scala").replace(".","/")+"/";
            String pojoName = clazz.getSimpleName()+".scala";
            String pojoScala = renderCase(clazz);
            writeFile(outDir+"/"+pojoPath+pojoName, pojoScala);
        }

    }

    private void writeFile(String pojoFile, String pojoScala) {
        try {
            File path = new File(pojoFile);
            File dir = path.getParentFile();
            if( !dir.exists() )
                dir.mkdirs();
            Files.write(Paths.get(pojoFile), pojoScala.getBytes(), StandardOpenOption.CREATE_NEW);
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

    public List<Class<?>> detectTraits(List<Class<?>> classes) {

        List<Class<?>> traits = Lists.newArrayList();

        for( Class clazz : classes ) {
            if (reflections.getSubTypesOf(clazz).size() > 0)
                traits.add(clazz);
        }

        return traits;
    }

    public List<Class<?>> detectCases(List<Class<?>> classes) {

        List<Class<?>> cases = Lists.newArrayList();

        for( Class clazz : classes ) {
            if (reflections.getSubTypesOf(clazz).size() == 0)
                cases.add(clazz);
        }

        return cases;
    }


    public String renderTrait(Class<?> pojoClass) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package ");
        stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
        stringBuffer.append(".traits");
        stringBuffer.append(LS);
        stringBuffer.append("trait "+pojoClass.getSimpleName());
        stringBuffer.append(" extends Serializable");
        stringBuffer.append(" {");

        Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
        appendFields(stringBuffer, fields, "def", ";");

        stringBuffer.append("}");

        return stringBuffer.toString();
    }

    public String renderClass(Class<?> pojoClass) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package ");
        stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
        stringBuffer.append(LS);
        stringBuffer.append("import org.apache.commons.lang.builder.{HashCodeBuilder, EqualsBuilder, ToStringBuilder}");
        stringBuffer.append(LS);
        stringBuffer.append("class "+pojoClass.getSimpleName());
        stringBuffer.append(" (");

        Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
        appendFields(stringBuffer, fields, "var", ",");

        stringBuffer.append(")");
        stringBuffer.append(" extends "+pojoClass.getPackage().getName().replace(".pojo.json", ".scala")+".traits."+pojoClass.getSimpleName());
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

    public String renderCase(Class<?> pojoClass) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("package ");
        stringBuffer.append(pojoClass.getPackage().getName().replace(".pojo.json", ".scala"));
        stringBuffer.append(LS);
        stringBuffer.append("case class "+pojoClass.getSimpleName());
        stringBuffer.append("(");
        Set<Field> fields = ReflectionUtils.getAllFields(pojoClass);
        appendFields(stringBuffer, fields, "var", ",");
        stringBuffer.append(")");
        if( pojoClass.getSuperclass() != null && !pojoClass.getSuperclass().equals(java.lang.Object.class)) {
            stringBuffer.append(" extends "+pojoClass.getSuperclass().getPackage().getName().replace(".pojo.json", ".scala")+".traits."+pojoClass.getSuperclass().getSimpleName());
        }
        stringBuffer.append(LS);

        return stringBuffer.toString();
    }

    private void appendFields(StringBuffer stringBuffer, Set<Field> fields, String varDef, String fieldDelimiter) {
        if( fields.size() > 0 ) {
            stringBuffer.append(LS);
            Map<String,Field> fieldsToAppend = uniqueFields(fields);
            for( Iterator<Field> iter = fieldsToAppend.values().iterator(); iter.hasNext(); ) {
                Field field = iter.next();
                if( override( field ) )
                    stringBuffer.append("override ");
                stringBuffer.append(varDef);
                stringBuffer.append(" ");
                stringBuffer.append(name(field));
                stringBuffer.append(": ");
                if( option(field) ) {
                    stringBuffer.append("scala.Option[");
                    stringBuffer.append(type(field));
                    stringBuffer.append("]");
                } else {
                    stringBuffer.append(type(field));
                }
                if( !fieldDelimiter.equals(";") && value(field) != null) {
                    stringBuffer.append(" = ");
                    if( option(field) ) {
                        stringBuffer.append("scala.Some(");
                        stringBuffer.append(value(field));
                        stringBuffer.append(")");
                    } else {
                        stringBuffer.append(value(field));
                    }
                }
                if( iter.hasNext()) stringBuffer.append(fieldDelimiter);
                stringBuffer.append(LS);
            }
        } else {
            stringBuffer.append(LS);
        }
    }

    private boolean option(Field field) {
        if( field.getName().equals("verb")) {
            return false;
        } else if( field.getType().equals(java.util.Map.class)) {
            return false;
        } else if( field.getType().equals(java.util.List.class)) {
            return false;
        } else return true;
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
            return "String";
        } else if( field.getType().equals(java.util.Map.class)) {
            return "scala.collection.mutable.Map[String,Any]";
        } else if( field.getType().equals(java.util.List.class)) {
            return "scala.collection.mutable.MutableList[Any]";
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
