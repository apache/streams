package org.apache.streams.data.moreover;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.Lists;
import com.moreover.api.Article;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.junit.Test;

import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test type conversions from xml string to article objects, and test type conversion from articles to activity objects.
 * Other types of conversions are allowed but are not recommended.  Those conversions are not tested.
 */
public class MoreoverTypeConverterTest {

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
    public void testXmlStringToArticleAndArticleToXmlString() {
        MoreoverTypeConverter convertToArticle = new MoreoverTypeConverter(MoreoverTypeConverter.Type.ARTICLE_XML_STRING, MoreoverTypeConverter.Type.ARTICLE_OBJECT);
        MoreoverTypeConverter convertToString = new MoreoverTypeConverter(MoreoverTypeConverter.Type.ARTICLE_OBJECT, MoreoverTypeConverter.Type.ARTICLE_XML_STRING);
        try(Scanner scanner = new Scanner(this.getClass().getResourceAsStream("/ArticleXML.txt"), "utf-8")) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(StringUtils.isNotEmpty(line)) {
                    List<StreamsDatum> articleResult = convertToArticle.process(new StreamsDatum(line));
                    assertNotNull(articleResult);
                    assertEquals(1, articleResult.size());
                    assertTrue(articleResult.get(0).getDocument() instanceof Article);
                    List<StreamsDatum> stringResult = convertToString.process(articleResult.get(0));
                    assertNotNull(stringResult);
                    assertEquals(1, stringResult.size());
                    assertEquals(line, stringResult.get(0).getDocument());
                }
            }
        }
    }

    @Test
    public void testArticleToActivityConversion() throws Exception {
        MoreoverTypeConverter converter = new MoreoverTypeConverter(MoreoverTypeConverter.Type.ARTICLE_OBJECT, MoreoverTypeConverter.Type.ACTIVITY_OBJECT);
        List<Article> articles = getArticles();
        for(Article article : articles) {
            List<StreamsDatum> result = converter.process(new StreamsDatum(article));
            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.get(0).getDocument() instanceof Activity);
        }
    }

    public List<Article> getArticles() throws Exception {
        List<Article> articles = Lists.newLinkedList();
        try(Scanner scanner = new Scanner(this.getClass().getResourceAsStream("/ArticleXML.txt"), "utf-8")) {
            while(scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(StringUtils.isNotEmpty(line)) {
                        articles.add(XML_MAPPER.readValue(line, Article.class));

                }
            }
        }
        return articles;
    }

}
