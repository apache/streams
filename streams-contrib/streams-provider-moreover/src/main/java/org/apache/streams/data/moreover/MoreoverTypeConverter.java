package org.apache.streams.data.moreover;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.moreover.api.Article;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Converts Moreover object reprsentation to other object formats.
 *
 * The only recommended conversions are from xml string to article or activity object
 * and article to xml string.  Other conversions are supported by not tested.
 */
public class MoreoverTypeConverter implements StreamsProcessor{

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreoverTypeConverter.class);
    private static final ObjectMapper JSON_MAPPER = StreamsJacksonMapper.getInstance();
    private static final XmlMapper XML_MAPPER;
    private static final MoreoverArticleSerializer ACTIVITY_CONVERTER = new MoreoverArticleSerializer();

    /**
     * Creates an xml mapper reading article xml
     */
    static {
        XmlFactory f = new XmlFactory(new InputFactoryImpl(),
                new OutputFactoryImpl());

        JacksonXmlModule module = new JacksonXmlModule();
        XML_MAPPER = new XmlMapper(f, module);
        XML_MAPPER.configure(
                DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                Boolean.TRUE);
        XML_MAPPER.configure(
                DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                Boolean.TRUE);
        XML_MAPPER.configure(
                DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY,
                Boolean.TRUE);
        XML_MAPPER.configure(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING,
                Boolean.TRUE);
        XML_MAPPER.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                Boolean.FALSE);
    }

    /**
     * Types to validate inputs and outputs
     */
    public enum Type {
        ARTICLE_XML_STRING(String.class, "ARTICLE_XML_STRING"),
        ARTICLE_JSON_STRING(String.class, "ARTICLE_JSON_STRING"),
        ACTIVITY_JSON_STRING(String.class, "ACTIVITY_JSON_STRING"),
        ACTIVITY_OBJECT(Activity.class, "ACTIVITY_OBJECT"),
        ARTICLE_OBJECT(Article.class, "ARTICLE_OBJECT");

        private final Class aClass;
        private final String string;
        Type(Class aClass, String string) {
            this.aClass = aClass;
            this.string = string;
        }


        @Override
        public String toString() {
            return this.string;
        }

        public Class getRepresentedClass() {
            return this.aClass;
        }
    }


    private Type inputType;
    private Type outputType;


    public MoreoverTypeConverter(Type inputType, Type outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
        if(this.inputType.equals(this.outputType)) {
            throw new RuntimeException("Input type is the same as the output type");
        } else if (inputType.equals(Type.ACTIVITY_OBJECT) && !outputType.equals(Type.ACTIVITY_JSON_STRING)) {
            throw new RuntimeException("Activity Objects can only be converted to Activity JSON strings. "+outputType);
        } else if (inputType.equals(Type.ACTIVITY_JSON_STRING) && !outputType.equals(Type.ACTIVITY_OBJECT)) {
            throw new RuntimeException("Activity Json string can only be convert to Activity Objects. "+outputType);
        }
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        Object input = entry.getDocument();
        if(!input.getClass().equals(this.inputType.getRepresentedClass())) {
            throw new RuntimeException("The documents class="+input.getClass()+" did not match the expected input="+this.inputType.getClass());
        }
        Object output = null;
        try {
            switch (this.inputType) {
                case ARTICLE_XML_STRING:
                    output = convertArticleXmlString((String) input, this.outputType);
                    break;
                case ARTICLE_JSON_STRING:
                    output = convertArticleJsonString((String) input, this.outputType);
                    break;
                case ARTICLE_OBJECT:
                    output = convertArticleObject((Article) input, this.outputType);
                    break;
                case ACTIVITY_JSON_STRING:
                    output = JSON_MAPPER.readValue((String) input, Activity.class);
                    break;
                case ACTIVITY_OBJECT:
                    output = JSON_MAPPER.writeValueAsString(input);
                    break;
                default:
                    break;
            }
        } catch (Throwable t) {
            LOGGER.warn("Unable to convert Moreover data : {}", t);
        }
        StreamsDatum datum = null;
        if(output instanceof String) { //remove new lines for S3 file writing
            output = ((String) output).replaceAll("(\r\n)|(\r)|(\n)|"+System.lineSeparator(), " ");
        }
        if(output instanceof Activity) {
            datum = new StreamsDatum(output, ((Activity) output).getId());
        } else if( output instanceof Article) {
            datum = new StreamsDatum(output, ((Article)output).getId());
        } else if( input instanceof Article) {
            datum = new StreamsDatum(output, ((Article)input).getId());
        } else if( input instanceof  Activity) {
            datum = new StreamsDatum(output, ((Activity)input).getId());
        }
        List<StreamsDatum> result = Lists.newLinkedList();
        if(datum != null) {
            result.add(datum);
        }
        return result;
    }

    protected Object convertArticleXmlString(String xmlString, Type outputType) throws IOException, ActivitySerializerException {
        Object result = null;
        Article article = XML_MAPPER.readValue(xmlString, Article.class);
        switch (outputType) {
            case ARTICLE_OBJECT:
                result = article;
                break;
            default:
                result = convertArticleObject(article, outputType);
                break;
        }
        return result;
    }

    protected Object convertArticleJsonString(String jsonString, Type outputType) throws IOException, ActivitySerializerException {
        Object result = null;
        Article article = JSON_MAPPER.readValue(jsonString, Article.class);
        switch (outputType) {
            case ARTICLE_OBJECT:
                result = article;
                break;
            default:
                result = convertArticleObject(article, outputType);
                break;
        }
        return result;
    }

    protected Object convertArticleObject(Article article, Type outputType) throws IOException, ActivitySerializerException {
        Object result = null;
        switch (outputType) {
            case ARTICLE_XML_STRING:
                result = XML_MAPPER.writeValueAsString(article);
                break;
            case ARTICLE_JSON_STRING:
                result = JSON_MAPPER.writeValueAsString(article);
                break;
            case ACTIVITY_OBJECT:
                result = ACTIVITY_CONVERTER.deserialize(article);
                break;
            case ACTIVITY_JSON_STRING:
                result = JSON_MAPPER.writeValueAsString(ACTIVITY_CONVERTER.deserialize(article));
                break;
        }
        return result;
    }




    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }
}
