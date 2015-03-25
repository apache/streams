package org.apache.streams.data.moreover;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.moreover.api.Article;
import com.moreover.api.ArticlesResponse;
import com.moreover.api.ObjectFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.pojo.json.Activity;
import org.junit.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.FileWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rebanks on 3/23/15.
 */
public class MoreoverSerializerTest {

    private static final XmlMapper XML_MAPPER;


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

    @Test
    public void testArticleToActivityConversion() throws Exception {
        List<Article> articles = getArticles();
        MoreoverArticleSerializer serializer = new MoreoverArticleSerializer();
        int counter = 0;
        for(Article article : articles ) {
            ++counter;
            Activity activity = serializer.deserialize(article);
            try {
                assertNotNull("Expected Activity to not be null" + counter, activity);
                assertEquals("Expected content to be equal", article.getContent(), activity.getContent());
                assertEquals("Expected published dates to be equal", RFC3339Utils.parseToUTC(RFC3339Utils.format(article.getPublishedDate())), activity.getPublished());
                assertEquals("Expected post verb", "POST".toLowerCase(), activity.getVerb().toLowerCase());
                assertNotNull(activity.getId());
                assertNotNull(activity.getActor());
                assertNotNull(activity.getProvider());
                assertNotNull(activity.getProvider().getAdditionalProperties().get("domain"));
            } catch (Throwable t) {
                System.out.printf("Failed on article number "+counter);
                throw t;
            }
        }
    }

    /**
     * Get data to test
     * @param args
     */
    public static void main(String[] args) throws Exception {
//        getRawResponseXML();
//        getArticleXML();
        System.out.println(getArticles().size());
    }

    public static List<Article> getArticles() throws Exception {
        List<Article> articles = Lists.newLinkedList();
        try(Scanner scanner = new Scanner(MoreoverSerializerTest.class.getResourceAsStream("/ArticleXML.txt"), "utf-8")) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(StringUtils.isNotEmpty(line)) {
                    try {
                        articles.add(XML_MAPPER.readValue(line, Article.class));
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        return articles;
    }

    //used to create MoreoverXMLResponse.xml file
    public static void getRawResponseXML() throws Exception {
        String urlString = "http://metabase.moreover.com/api/v10/articles?key=0b335da8a0aa4bca9e1771244be82abf&limit=500&sequence_id=0";
        try(FileWriter writer = new FileWriter("MoreoverXMLResponse.xml")) {
            URL url = new URL(urlString);
            IOUtils.copy(url.openStream(), writer, Charset.forName("UTF-8"));
        }
    }

    //used to create ArticleXML.txt file and verify that strings can be converted back to article objects.
    public static void getArticleXML() throws Exception {
        StringWriter writer = new StringWriter();
        IOUtils.copy(MoreoverSerializerTest.class.getResourceAsStream("/MoreoverXMLResponse.xml"), writer, Charset.forName("UTF-8"));
        System.out.println(writer.toString());
        MoreoverResult result = new MoreoverResult("", writer.toString(), 0, 0);
        result.process();
        try(FileWriter fileWriter = new FileWriter("ArticleXML.txt")) {
            for(Article article : result.getArticles()) {
                String xmlString = XML_MAPPER.writeValueAsString(article);
                article = XML_MAPPER.readValue(xmlString, Article.class);
                xmlString = xmlString.replaceAll("\r\n", " ");
                article = XML_MAPPER.readValue(xmlString, Article.class);
                xmlString = xmlString.replaceAll("(\r)|(\n)", " ");
                xmlString = xmlString.replaceAll(System.lineSeparator(), " ");
                article = XML_MAPPER.readValue(xmlString, Article.class);
                fileWriter.write(xmlString+"\n");
            }
        }
    }

}
