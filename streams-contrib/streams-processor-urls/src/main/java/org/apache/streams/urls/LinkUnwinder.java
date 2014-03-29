package org.apache.streams.urls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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

public class LinkUnwinder implements Link
{
    private final static Logger LOGGER = LoggerFactory.getLogger(LinkUnwinder.class);

    private static final int MAX_ALLOWED_REDIRECTS = 30;
    private static final int DEFAULT_HTTP_TIMEOUT = 5000; //originally 30000
    private static final String LOCATION_IDENTIFIER = "location";
    private static final String SET_COOKIE_IDENTIFIER = "set-cookie";

    private Date startTime = new Date();
    private String originalURL;
    private LinkStatus status;
    private String finalURL;
    private String domain;
    private boolean wasRedirected;
    private List<String> redirects = new ArrayList<String>();
    private boolean isTracked = false;
    private int finalResponseCode;
    private Collection<String> cookies;

    private String normalizedUrl;
    private List<String> urlParts;

    private int redirectCount = 0;
    private long tookInMillis = 0;

    private static final Collection<String> BOTS_ARE_OK = new ArrayList<String>() {{
       add("t.co");
    }};

    private static final Collection<String> URL_TRACKING_TO_REMOVE = new ArrayList<String>() {{
        /******************************************************************
         * Google uses parameters in the URL string to track referrers
         * on their Google Analytics and promotions. These are the
         * identified URL patterns.
         *
         * URL:
         * https://support.google.com/analytics/answer/1033867?hl=en
         *****************************************************************/

        // Required. Use utm_source to identify a search engine, newsletter name, or other source.
        add("([\\?&])utm_source(=)[^&?]*");

        // Required. Use utm_medium to identify a medium such as email or cost-per- click.
        add("([\\?&])utm_medium(=)[^&?]*");

        // Used for paid search. Use utm_term to note the keywords for this ad.
        add("([\\?&])utm_term(=)[^&?]*");

        // Used for A/B testing and content-targeted ads. Use utm_content to differentiate ads or links that point to the same
        add("([\\?&])utm_content(=)[^&?]*");

        // Used for keyword analysis. Use utm_campaign to identify a specific product promotion or strategic campaign.
        add("([\\?&])utm_campaign(=)[^&?]*");
    }};

    public boolean isFailure()              { return false; }
    public String getOriginalURL()          { return this.originalURL; }
    public LinkStatus getStatus()           { return this.status; }
    public String getDomain()               { return this.domain; }
    public String getFinalURL()             { return this.finalURL; }
    public List<String> getRedirects()      { return this.redirects; }
    public boolean wasRedirected()          { return this.wasRedirected; }
    public boolean isTracked()              { return this.isTracked; }
    public String getFinalResponseCode()    { return Integer.toString(this.finalResponseCode); }
    public long getTookInMillis()           { return this.tookInMillis; }
    public String getNormalizedURL()        { return this.normalizedUrl; }
    public List<String> getUrlParts()       { return this.urlParts; }

    public LinkUnwinder(String originalURL) {
        this.originalURL = originalURL;
    }

    public void run() {
        // we are going to try three times just incase we catch the service off-guard
        // this is mainly to help us with our tests.
        for(int i = 0; (i < 3) && this.finalURL == null ; i++) {
            if(this.status != LinkStatus.SUCCESS)
                unwindLink(this.originalURL);
        }
        this.finalURL = cleanURL(this.finalURL);
        this.normalizedUrl = normalizeURL(this.finalURL);
        this.urlParts = tokenizeURL(this.normalizedUrl);

        this.updateTookInMillis();
    }

    protected void updateTookInMillis() {
        this.tookInMillis = new Date().getTime() - this.startTime.getTime();
    }

    public void unwindLink(String url)
    {
        // Check to see if they wound up in a redirect loop
        if((this.redirectCount > 0 && (this.originalURL.equals(url) || this.redirects.contains(url))) || (this.redirectCount > MAX_ALLOWED_REDIRECTS))
        {
            this.status = LinkStatus.LOOP;
            return;
        }

        if(!this.originalURL.equals(url))
            this.redirects.add(url);

        HttpURLConnection connection = null;

        try
        {
            URL thisURL = new URL(url);
            connection = (HttpURLConnection)new URL(url).openConnection();

            // now we are going to pretend that we are a browser...
            // This is the way my mac works.
            if(!BOTS_ARE_OK.contains(thisURL.getHost()))
            {
                connection.addRequestProperty("Host", thisURL.getHost());
                connection.addRequestProperty("Connection", "Keep-Alive");
                connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36");
                connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8,zh;q=0.6");

                // the test to seattlemamadoc.com prompted this change.
                // they auto detect bots by checking the referrer chain and the 'user-agent'
                // this broke the t.co test. t.co URLs are EXPLICITLY ok with bots
                // there is a list for URLS that behave this way at the top in BOTS_ARE_OK
                // smashew 2013-13-2013

                if(this.redirectCount > 0 && BOTS_ARE_OK.contains(thisURL.getHost()))
                    connection.addRequestProperty("Referrer", this.originalURL);
            }

            connection.setReadTimeout(DEFAULT_HTTP_TIMEOUT);
            connection.setConnectTimeout(DEFAULT_HTTP_TIMEOUT);

            connection.setInstanceFollowRedirects(false);

            if(this.cookies != null)
                for (String cookie : cookies)
                    connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);

            connection.connect();

            this.finalResponseCode = connection.getResponseCode();

            /**************
             *
             */
            Map<String,List<String>> headers = createCaseInsenitiveMap(connection.getHeaderFields());
            /******************************************************************
             * If they want us to set cookies, well, then we will set cookies
             * Example URL:
             * http://nyti.ms/1bCpesx
             *****************************************************************/
            if(headers.containsKey(SET_COOKIE_IDENTIFIER))
                this.cookies = headers.get(SET_COOKIE_IDENTIFIER);

            switch (this.finalResponseCode)
            {
                case 200: // HTTP OK
                    this.finalURL = connection.getURL().toString();
                    this.domain = new URL(this.finalURL).getHost();
                    this.status = LinkStatus.SUCCESS;
                    break;
                case 300: // Multiple choices
                case 301: // URI has been moved permanently
                case 302: // Found
                case 303: // Primarily for a HTTP Post
                case 304: // Not Modified
                case 306: // This status code is unused but in the redirect block.
                case 307: // Temporary re-direct
                    /*******************************************************************
                     * Author:
                     * Smashew
                     *
                     * Date: 2013-11-15
                     *
                     * Note:
                     * It is possible that we have already found our final URL. In
                     * the event that we have found our final URL, we are going to
                     * save this URL as long as it isn't the original URL.
                     * We are still going to ask the browser to re-direct, but in the
                     * case of yet another redirect, seen with the redbull test
                     * this can be followed by a 304, a browser, by W3C standards would
                     * still render the page with it's content, but for us to assert
                     * a success, we are really hoping for a 304 message.
                     *******************************************************************/
                    if(!this.originalURL.toLowerCase().equals(connection.getURL().toString().toLowerCase()))
                        this.finalURL = connection.getURL().toString();
                    if(!headers.containsKey(LOCATION_IDENTIFIER))
                    {
                        LOGGER.info("Headers: {}", headers);
                        this.status = LinkStatus.REDIRECT_ERROR;
                    }
                    else
                    {
                        this.wasRedirected = true;
                        this.redirectCount++;
                        unwindLink(connection.getHeaderField(LOCATION_IDENTIFIER));
                    }
                    break;
                case 305: // User must use the specified proxy (deprecated by W3C)
                    break;
                case 401: // Unauthorized (nothing we can do here)
                    this.status = LinkStatus.UNAUTHORIZED;
                    break;
                case 403: // HTTP Forbidden (Nothing we can do here)
                    this.status = LinkStatus.FORBIDDEN;
                    break;
                case 404: // Not Found (Page is not found, nothing we can do with a 404)
                    this.status = LinkStatus.NOT_FOUND;
                    break;
                case 500: // Internal Server Error
                case 501: // Not Implemented
                case 502: // Bad Gateway
                case 503: // Service Unavailable
                case 504: // Gateway Timeout
                case 505: // Version not supported
                    this.status = LinkStatus.HTTP_ERROR_STATUS;
                    break;
                default:
                    LOGGER.info("Unrecognized HTTP Response Code: {}", this.finalResponseCode);
                    this.status = LinkStatus.NOT_FOUND;
                    break;
            }
        }
        catch (MalformedURLException e)
        {
            // the URL is trash, so, it can't load it.
            this.status = LinkStatus.MALFORMED_URL;
        }
        catch (IOException ex)
        {
            // there was an issue we are going to set to error.
            this.status = LinkStatus.ERROR;
        }
        catch (Exception ex)
        {
            // there was an unknown issue we are going to set to exception.
            this.status = LinkStatus.EXCEPTION;
        }
        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }

    private Map<String,List<String>> createCaseInsenitiveMap(Map<String,List<String>> input) {
        Map<String,List<String>> toReturn = new HashMap<String, List<String>>();
        for(String k : input.keySet())
            if(k != null && input.get(k) != null)
                toReturn.put(k.toLowerCase(), input.get(k));
        return toReturn;
    }

    private String cleanURL(String url)
    {
        // If they pass us a null URL then we are going to pass that right back to them.
        if(url == null)
            return null;

        // remember how big the URL was at the start
        int startLength = url.length();

        // Iterate through all the known URL parameters of tracking URLs
        for(String pattern : URL_TRACKING_TO_REMOVE)
            url = url.replaceAll(pattern, "");

        // If the URL is smaller than when it came in. Then it had tracking information
        if(url.length() < startLength)
            this.isTracked = true;

        // return our url.
        return url;
    }

    /**
     * Removes the protocol, if it exists, from the front and
     * removes any random encoding characters
     * Extend this to do other url cleaning/pre-processing
     * @param url - The String URL to normalize
     * @return normalizedUrl - The String URL that has no junk or surprises
     */
    public static String normalizeURL(String url)
    {
        // Decode URL to remove any %20 type stuff
        String normalizedUrl = url;
        try {
            // I've used a URLDecoder that's part of Java here,
            // but this functionality exists in most modern languages
            // and is universally called url decoding
            normalizedUrl = URLDecoder.decode(url, "UTF-8");
        }
        catch(UnsupportedEncodingException uee)
        {
            System.err.println("Unable to Decode URL. Decoding skipped.");
            uee.printStackTrace();
        }

        // Remove the protocol, http:// ftp:// or similar from the front
        if (normalizedUrl.contains("://"))
            normalizedUrl = normalizedUrl.split(":/{2}")[1];

        // Room here to do more pre-processing

        return normalizedUrl;
    }

    /**
     * Goal is to get the different parts of the URL path. This can be used
     * in a classifier to help us determine if we are working with
     *
     * Reference:
     * http://stackoverflow.com/questions/10046178/pattern-matching-for-url-classification
     * @param url - Url to be tokenized
     * @return tokens - A String array of all the tokens
     */
    public static List<String> tokenizeURL(String url)
    {
        url = normalizeURL(url);
        // I assume that we're going to use the whole URL to find tokens in
        // If you want to just look in the GET parameters, or you want to ignore the domain
        // or you want to use the domain as a token itself, that would have to be
        // processed above the next line, and only the remaining parts split
        List<String> toReturn = new ArrayList<String>();

        // Split the URL by forward slashes. Most modern browsers will accept a URL
        // this malformed such as http://www.smashew.com/hello//how////are/you
        // hence the '+' in the regular expression.
        for(String part: url.split("/+"))
            toReturn.add(part.toLowerCase());

        // return our object.
        return toReturn;

        // One could alternatively use a more complex regex to remove more invalid matches
        // but this is subject to your (?:in)?ability to actually write the regex you want

        // These next two get rid of tokens that are too short, also.

        // Destroys anything that's not alphanumeric and things that are
        // alphanumeric but only 1 character long
        //String[] tokens = url.split("(?:[\\W_]+\\w)*[\\W_]+");

        // Destroys anything that's not alphanumeric and things that are
        // alphanumeric but only 1 or 2 characters long
        //String[] tokens = url.split("(?:[\\W_]+\\w{1,2})*[\\W_]+");
    }


}