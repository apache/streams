package org.apache.streams.tika;

import org.apache.streams.urls.LinkUnwinder;
import org.apache.streams.util.DateUtil;
import org.apache.streams.tika.BoilerPipeArticle;
import org.apache.streams.tika.LanguageDetected;
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

import de.l3s.boilerpipe.document.TextBlock;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.*;


/**
 * Helpful resources for this class:
 *
 * // TODO: This needs to be rethought.
 *
 * URL:
 * Tika UI: http://www.apache.org/dyn/closer.cgi/tika/tika-app-1.4.jar
 * Tika: http://tika.apache.org/
 * Dublin Core: http://dublincore.org/documents/dces/
 */

public class LinkExpander extends LinkUnwinder
{
    private final static Logger LOGGER = LoggerFactory.getLogger(LinkExpander.class);

    private static final AutoDetectParser AUTO_DETECT_PARSER = new AutoDetectParser();

    private final Map<String, String> metaData = new HashMap<String, String>();

    private final Set<String> keywords = new HashSet<String>();

    private BoilerPipeArticle article = new BoilerPipeArticle();

    // sblackmon: I put this here so I wouldn't get NullPointerExceptions when serializing results
    public TextBlock getContentTextBlock() {
        for(TextBlock textBlock : article.getTextBlocks())
            if(textBlock.isContent())
                return textBlock;
        return null;
    }

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


    public LinkExpander(String url) {
        super(url);
    }

    public void run() {
        super.run();
        expandLink();
    }


    private void expandLink()
    {
        InputStream is = null;

        try
        {
            URL url = new URL(this.getFinalURL());
            URLConnection con = url.openConnection();
            con.setConnectTimeout(10000);
            is = con.getInputStream();

            parseMainContent(is);
            parsePlainText(is);
            detectLanguage(article.getPlainText());

        }
        // Handle all Exceptions by just reporting that the site status was an error.
        catch (IOException e) {
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.ERROR);
        }
        catch (TikaException e) {
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.ERROR);
        }
        catch (SAXException e) {
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.ERROR);
        }
        catch (Exception e) {
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.ERROR);
        }
        finally {
            if (!(is == null)) {
                try {
                    is.close();
                }
                catch(IOException e) {
                    LOGGER.warn("Problem closing the input stream: {}", e.getMessage());
                }
            }
        }
    }

    private void parseMainContent(InputStream is) throws IOException, SAXException, TikaException, ParseException
    {
        Metadata rawMetaData = new Metadata();
        StringWriter stringWriter = new StringWriter();

        BoilerpipeContentHandler boilerpipeContentHandler = new BoilerpipeContentHandler(stringWriter);

        AUTO_DETECT_PARSER.parse(is,
                boilerpipeContentHandler,
                rawMetaData);

        article.setTextBlocks(boilerpipeContentHandler.getTextDocument().getTextBlocks());
        article.setBody(boilerpipeContentHandler.getTextDocument().getContent());
        article.setTitle(boilerpipeContentHandler.getTextDocument().getTitle());

        // this map is for ourselves so we convert it to lower-case to make it easier to search.
        // the meta data that is going to be returned will be unmodified meta data.
        for(String name : rawMetaData.names())
            if(rawMetaData.get(name) != null) {
                this.metaData.put(name.toLowerCase(), rawMetaData.get(name));
                article.setAdditionalProperty(name.toLowerCase(), rawMetaData.get(name));
            }

        article.setAuthor(metaDataSearcher(LinkExpander.AUTHOR_SEARCH));
        article.setDescription(metaDataSearcher(LinkExpander.DESCRIPTION_SEARCH));
        article.setMedium(metaDataSearcher(LinkExpander.MEDIUM_SEARCH));
        article.setImageURL(metaDataSearcher(LinkExpander.IMAGE_SEARCH));
        article.setLocale(metaDataSearcher(LinkExpander.LOCALE_SEARCH));

        article.setFacebookApp(metaDataSearcher(LinkExpander.FACEBOOK_APP_SEARCH));
        article.setFacebookPage(metaDataSearcher(LinkExpander.FACEBOOK_PAGE_SEARCH));

        article.setTwitterCreator(metaDataSearcher(LinkExpander.TWITTER_CREATOR_SEARCH));
        article.setTwitterSite(metaDataSearcher(LinkExpander.TWITTER_SITE_SEARCH));

        mergeSet(LinkExpander.KEYWORDS_SEARCH, this.keywords);

        article.setPublishedDate(DateUtil.determineDate(metaDataSearcher(LinkExpander.PUB_DATE_SEARCH)));
        article.setLastModifiedDate(DateUtil.determineDate(metaDataSearcher(LinkExpander.MODIFIED_DATE_SEARCH)));

        if(article.getBody().length() > 50)
            article.setSiteStatus(BoilerPipeArticle.SiteStatus.SUCCESS);
    }

    private void parsePlainText(InputStream is) throws Exception {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        new HtmlParser().parse(is, handler, metadata, new ParseContext());
        article.setPlainText(handler.toString());
    }

    private void detectLanguage(String plainText) throws Exception {
        LanguageDetected languageDetected = new LanguageDetected();
        LanguageIdentifier languageIdentifier = new LanguageIdentifier(plainText);
        languageDetected.setLanguageCode(languageIdentifier.getLanguage());
        languageDetected.setIsLanguageReasonablyCertain(languageIdentifier.isReasonablyCertain());
        article.setLanguageDetected(languageDetected);
    }

    private String metaDataSearcher(Collection<String> itemsToSearch) {
        for(String s : itemsToSearch)
            if(this.metaData.containsKey(s))
                return this.metaData.get(s);

        // the meta searcher returned nothing.
        return null;
    }

    private void mergeSet(Collection<String> itemsToSearch, Set<String> set) {
        for(String s : itemsToSearch)
            Collections.addAll(set, s == null || s.equals("") ? new String[]{} : s.split(","));
    }

}
