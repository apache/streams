package org.apache.streams.tika;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import de.l3s.boilerpipe.document.TextBlock;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.urls.LinkCrawler;
import org.apache.streams.urls.LinkCrawlerProcessor;
import org.apache.streams.urls.LinkDetails;
import org.apache.streams.util.DateUtil;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.BoilerpipeContentHandler;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class TikaProcessor implements StreamsProcessor
{
    private final static String STREAMS_ID = "TikaProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(TikaProcessor.class);

    private AutoDetectParser autoDetectParser;

    private ObjectMapper mapper;

    protected LanguageDetails language;
    protected BoilerPipeArticle article;

    private static final Collection<String> AUTHOR_SEARCH = new ArrayList<String>() {{
        add("og:author");
        add("dc:author");
        add("author");
    }};

    private static final Collection<String> DESCRIPTION_SEARCH = new ArrayList<String>() {{
        add("og:description");
        add("dc:description");
        add("description");
    }};

    private static final Collection<String> MEDIUM_SEARCH = new ArrayList<String>() {{
        add("og:medium");
        add("dc:medium");
        add("medium");
    }};

    private static final Collection<String> IMAGE_SEARCH = new ArrayList<String>() {{
        add("og:image");
        add("twitter:image");
        add("image");
    }};

    private static final Collection<String> KEYWORDS_SEARCH = new ArrayList<String>() {{
        add("keywords");
        add("news_keywords");
    }};

    private static final Collection<String> PUB_DATE_SEARCH = new ArrayList<String>() {{
        add("pubdate");
        add("os:pubdate");
        add("dc:pubdate");
    }};

    private static final Collection<String> MODIFIED_DATE_SEARCH = new ArrayList<String>() {{
        add("lastmod");
        add("last-modified");
    }};

    private static final Collection<String> LOCALE_SEARCH = new ArrayList<String>() {{
        add("locale");
        add("os:locale");
        add("dc:local");
    }};

    // Social Searchers
    private static final Collection<String> FACEBOOK_PAGE_SEARCH = new ArrayList<String>() {{
        add("fb:page_id");
    }};

    private static final Collection<String> FACEBOOK_APP_SEARCH = new ArrayList<String>() {{
        add("fb:app_id");
    }};

    private static final Collection<String> TWITTER_SITE_SEARCH = new ArrayList<String>() {{
        add("twitter:site:id");
        add("twitter:site");
    }};

    private static final Collection<String> TWITTER_CREATOR_SEARCH = new ArrayList<String>() {{
        add("twitter:creator:id");
        add("twitter:creator");
    }};

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        LOGGER.debug("{} processing {}", STREAMS_ID, entry.getDocument().getClass());

        Activity activity;

        System.out.println( STREAMS_ID + " processing " + entry.getDocument().getClass());
        // get list of shared urls
        if( entry.getDocument() instanceof Activity) {

            activity = (Activity) entry.getDocument();

        }
        else if(entry.getDocument() instanceof String) {

            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.warn(e.getMessage());
                return(Lists.newArrayList(entry));
            }

        }
        else throw new NotImplementedException();

        Object crawlerResult = activity.getExtensions().getAdditionalProperties().get(LinkCrawlerProcessor.STREAMS_ID);

        LinkDetails linkDetails = mapper.convertValue(crawlerResult, LinkDetails.class);

        try {
            parsePlainText(linkDetails.getContent());
            detectLanguage(article.getPlainText());
            parseMainContent(article.getPlainText());
        } catch( Exception e ) {
            LOGGER.warn( e.getMessage() );
        }

        return result;
    }


    @Override
    public void prepare(Object o) {
        this.mapper = StreamsJacksonMapper.getInstance();
        this.mapper.registerModule(new JsonOrgModule());
        this.autoDetectParser = new AutoDetectParser();
    }

    @Override
    public void cleanUp() {

    }

    private void parsePlainText(String content) throws Exception {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        InputStream is = new ByteArrayInputStream(content.getBytes());
        new HtmlParser().parse(is, handler, metadata, new ParseContext());
        language = new LanguageDetails();

        article.setPlainText(handler.toString());

    }

    private void detectLanguage(String plainText) throws Exception {

        LanguageIdentifier languageIdentifier = new LanguageIdentifier(plainText);
        LanguageDetails languageDetails = new LanguageDetails();
        languageDetails.setLanguageCode(languageIdentifier.getLanguage());
        languageDetails.setIsLanguageReasonablyCertain(languageIdentifier.isReasonablyCertain());
        language = languageDetails;

    }

    private void parseMainContent(String plainText) throws IOException, SAXException, TikaException, ParseException
    {
        Metadata rawMetaData = new Metadata();
        StringWriter stringWriter = new StringWriter();

        BoilerpipeContentHandler boilerpipeContentHandler = new BoilerpipeContentHandler(stringWriter);

        InputStream is = new ByteArrayInputStream(plainText.getBytes());

        autoDetectParser.parse(is,
                boilerpipeContentHandler,
                rawMetaData);

        article.setBody(boilerpipeContentHandler.getTextDocument().getContent());
        article.setTitle(boilerpipeContentHandler.getTextDocument().getTitle());

        // this map is for ourselves so we convert it to lower-case to make it easier to search.
        // the meta data that is going to be returned will be unmodified meta data.
        for(String name : rawMetaData.names())
            if(rawMetaData.get(name) != null)
                article.getAdditionalProperties().put(name.toLowerCase(), rawMetaData.get(name));

        article.setAuthor(metaDataSearcher(AUTHOR_SEARCH));
        article.setDescription(metaDataSearcher(DESCRIPTION_SEARCH));
        article.setMedium(metaDataSearcher(MEDIUM_SEARCH));
        article.setImageURL(metaDataSearcher(IMAGE_SEARCH));
        article.setLocale(metaDataSearcher(LOCALE_SEARCH));

        mergeSet(KEYWORDS_SEARCH, article.getKeywords());

        article.setPublishedDate(DateUtil.determineDate(metaDataSearcher(PUB_DATE_SEARCH)));
        article.setLastModifiedDate(DateUtil.determineDate(metaDataSearcher(MODIFIED_DATE_SEARCH)));

        if(article.getBody().length() > 50)
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.SUCCESS);
    }

    private String metaDataSearcher(Collection<String> itemsToSearch) {
        for(String s : itemsToSearch)
            if(article.getAdditionalProperties().containsKey(s))
                return (String) article.getAdditionalProperties().get(s);

        // the meta searcher returned nothing.
        return null;
    }

    private void mergeSet(Collection<String> itemsToSearch, Set<String> set) {
        for(String s : itemsToSearch)
            Collections.addAll(set, s == null || s.equals("") ? new String[]{} : s.split(","));
    }

}