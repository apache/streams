/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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
package org.apache.streams.urls;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

public class LinkResolver implements Serializable {

    /**
     * References:
     * Some helpful references to demonstrate the different types of browser re-directs that
     * can happen. If you notice a redirect that was not followed to the proper place please
     * submit a bug at :
     * https://issues.apache.org/jira/browse/STREAMS
     * <p/>
     * Purpose              URL
     * -------------        ----------------------------------------------------------------
     * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
     * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
     * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
     */


    private final static Logger LOGGER = LoggerFactory.getLogger(LinkResolver.class);

    private static final int MAX_ALLOWED_REDIRECTS = 30;                // We will only chase the link to it's final destination a max of 30 times.
    private static final int DEFAULT_HTTP_TIMEOUT = 10000;              // We will only wait a max of 10,000 milliseconds (10 seconds) for any HTTP response
    private static final String LOCATION_IDENTIFIER = "location";
    private static final String SET_COOKIE_IDENTIFIER = "set-cookie";

    // if Bots are not 'ok' this is the spoof settings that we'll use
    private static final Map<String, String> SPOOF_HTTP_HEADERS = new HashMap<String, String>() {{
        put("Connection", "Keep-Alive");
        put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.48 Safari/537.36");
        put("Accept-Language", "en-US,en;q=0.8,zh;q=0.6");
    }};

    // These are the known domains that are 'bot' friendly.
    private static final Collection<String> BOTS_ARE_OK = new ArrayList<String>() {{
        add("t.co");
    }};

    // To help canonicalize the URL, these parts are 'known' to be 'ok' to remove
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


    // This element holds all the information about all the re-directs that have taken place
    // and the steps and HTTP codes that occurred inside of each step.
    private final LinkDetails linkDetails;
    private Collection<String> domainsSensitiveTo = new HashSet<String>();

    /**
     * Get the link details
     *
     * @return Detailed log of every redirection that took place with the browser along with it it's ultimate status code.
     */
    public LinkDetails getLinkDetails() {
        return linkDetails;
    }

    /**
     * Raw string input of the URL. If the URL is invalid, the response code that is returned will indicate such.
     *
     * @param originalURL The URL you wish to unwind represented as a string.
     */
    public LinkResolver(String originalURL) {
        linkDetails = new LinkDetails();
        linkDetails.setOriginalURL(originalURL);
    }

    public void run() {

        Preconditions.checkNotNull(linkDetails.getOriginalURL());

        linkDetails.setStartTime(DateTime.now());

        // we are going to try three times just in case we catch a slow server or one that needs
        // to be warmed up. This tends to happen many times with smaller private servers
        for (int i = 0; (i < 3) && linkDetails.getFinalURL() == null; i++)
            if (linkDetails.getLinkStatus() != LinkDetails.LinkStatus.SUCCESS)
                unwindLink(linkDetails.getOriginalURL());

        // because this is a POJO we need to make sure that we set this to false if it was never re-directed
        if(this.linkDetails.getRedirectCount() == 0 || this.linkDetails.getRedirected() == null)
            this.linkDetails.setRedirected(false);

        linkDetails.setFinalURL(cleanURL(linkDetails.getFinalURL()));
        linkDetails.setNormalizedURL(normalizeURL(linkDetails.getFinalURL()));
        linkDetails.setUrlParts(tokenizeURL(linkDetails.getNormalizedURL()));

        this.updateTookInMillis();
    }

    protected void updateTookInMillis() {
        Preconditions.checkNotNull(linkDetails.getStartTime());
        linkDetails.setTookInMills(DateTime.now().minus(linkDetails.getStartTime().getMillis()).getMillis());
    }

    public void unwindLink(String url) {
        Preconditions.checkNotNull(linkDetails);
        Preconditions.checkNotNull(url);

        // Check to see if they wound up in a redirect loop,
        // IE: 'A' redirects to 'B', then 'B' redirects to 'A'
        if ((linkDetails.getRedirectCount() != null && linkDetails.getRedirectCount() > 0 &&
                (linkDetails.getOriginalURL().equals(url) || linkDetails.getRedirects().contains(url)))
                || (linkDetails.getRedirectCount() != null && linkDetails.getRedirectCount() > MAX_ALLOWED_REDIRECTS)) {
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.LOOP);
            return;
        }

        if (!linkDetails.getOriginalURL().equals(url))
            linkDetails.getRedirects().add(url);

        HttpURLConnection connection = null;

        // Store where the redirected link will go (if there is one)
        String reDirectedLink = null;

        try {
            // Turn the string into a URL
            URL thisURL = new URL(url);

            // Be sensitive to overloading domains STREAMS-77
            try {
                String host = thisURL.getHost().toLowerCase();
                if(!domainsSensitiveTo.contains(host)) {
                    domainsSensitiveTo.add(host);
                    long domainWait = LinkResolverHelperFunctions.waitTimeForDomain(thisURL.getHost());
                    if (domainWait > 0) {
                        LOGGER.debug("Waiting for domain: {}", domainWait);
                        Thread.sleep(domainWait);
                    }
                }
            } catch(Exception e) {
                // noOp
            }

            connection = (HttpURLConnection) new URL(url).openConnection();

            // now we are going to pretend that we are a browser...
            // This is the way my mac works.
            if (!BOTS_ARE_OK.contains(thisURL.getHost())) {
                connection.addRequestProperty("Host", thisURL.getHost());

                // Bots are not 'ok', so we need to spoof the headers
                for (String k : SPOOF_HTTP_HEADERS.keySet())
                    connection.addRequestProperty(k, SPOOF_HTTP_HEADERS.get(k));

                // the test to seattlemamadoc.com prompted this change.
                // they auto detect bots by checking the referrer chain and the 'user-agent'
                // this broke the t.co test. t.co URLs are EXPLICITLY ok with bots
                // there is a list for URLS that behave this way at the top in BOTS_ARE_OK
                // smashew 2013-13-2013
                if (linkDetails.getRedirectCount() > 0 && BOTS_ARE_OK.contains(thisURL.getHost()))
                    connection.addRequestProperty("Referrer", linkDetails.getOriginalURL());
            }

            connection.setReadTimeout(DEFAULT_HTTP_TIMEOUT);
            connection.setConnectTimeout(DEFAULT_HTTP_TIMEOUT);

            // we want to follow this behavior on our own to ensure that we are getting to the
            // proper place. This is especially true with links that are wounded by special
            // link winders,
            // IE:
            connection.setInstanceFollowRedirects(false);

            if (linkDetails.getCookies() != null)
                for (String cookie : linkDetails.getCookies())
                    connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);

            connection.connect();

            linkDetails.setFinalResponseCode((long) connection.getResponseCode());

            Map<String, List<String>> headers = createCaseInsensitiveMap(connection.getHeaderFields());
            /******************************************************************
             * If they want us to set cookies, well, then we will set cookies
             * Example URL:
             * http://nyti.ms/1bCpesx
             *****************************************************************/
            if (headers.containsKey(SET_COOKIE_IDENTIFIER))
                linkDetails.getCookies().add(headers.get(SET_COOKIE_IDENTIFIER).get(0));

            switch (linkDetails.getFinalResponseCode().intValue()) {
                /**
                 * W3C HTTP Response Codes:
                 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                 */
                case 200: // HTTP OK
                    linkDetails.setFinalURL(connection.getURL().toString());
                    linkDetails.setDomain(new URL(linkDetails.getFinalURL()).getHost());
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.SUCCESS);
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
                    if (!linkDetails.getOriginalURL().toLowerCase().equals(connection.getURL().toString().toLowerCase()))
                        linkDetails.setFinalURL(connection.getURL().toString());
                    if (!headers.containsKey(LOCATION_IDENTIFIER)) {
                        LOGGER.info("Headers: {}", headers);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.REDIRECT_ERROR);
                    } else {
                        linkDetails.setRedirected(Boolean.TRUE);
                        linkDetails.setRedirectCount(linkDetails.getRedirectCount() + 1);
                        reDirectedLink = connection.getHeaderField(LOCATION_IDENTIFIER);
                    }
                    break;
                case 305: // User must use the specified proxy (deprecated by W3C)
                    break;
                case 401: // Unauthorized (nothing we can do here)
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.UNAUTHORIZED);
                    break;
                case 403: // HTTP Forbidden (Nothing we can do here)
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.FORBIDDEN);
                    break;
                case 404: // Not Found (Page is not found, nothing we can do with a 404)
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.NOT_FOUND);
                    break;
                case 500: // Internal Server Error
                case 501: // Not Implemented
                case 502: // Bad Gateway
                case 503: // Service Unavailable
                case 504: // Gateway Timeout
                case 505: // Version not supported
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.HTTP_ERROR_STATUS);
                    break;
                default:
                    LOGGER.info("Unrecognized HTTP Response Code: {}", linkDetails.getFinalResponseCode());
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.NOT_FOUND);
                    break;
            }
        } catch (MalformedURLException e) {
            // the URL is trash, so, it can't load it.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.MALFORMED_URL);
        } catch (IOException ex) {
            // there was an issue we are going to set to error.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
        } catch (Exception ex) {
            // there was an unknown issue we are going to set to exception.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.EXCEPTION);
        } finally {
            // if the connection is not null, then we need to disconnect to close any underlying resources
            if (connection != null)
                connection.disconnect();
        }

        // If there was a redirection, then we have to keep going
        // Placing this code here should help to satisfy ensuring that the connection object
        // is closed successfully.
        if (reDirectedLink != null)
            unwindLink(reDirectedLink);

    }

    private Map<String, List<String>> createCaseInsensitiveMap(Map<String, List<String>> input) {
        Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
        for (String k : input.keySet())
            if (k != null && input.get(k) != null)
                toReturn.put(k.toLowerCase(), input.get(k));
        return toReturn;
    }

    private String cleanURL(String url) {
        // If they pass us a null URL then we are going to pass that right back to them.
        if (url == null)
            return null;

        // remember how big the URL was at the start
        int startLength = url.length();

        // Iterate through all the known URL parameters of tracking URLs
        for (String pattern : URL_TRACKING_TO_REMOVE)
            url = url.replaceAll(pattern, "");

        // If the URL is smaller than when it came in. Then it had tracking information
        if (url.length() < startLength)
            linkDetails.setTracked(Boolean.TRUE);

        // return our url.
        return url;
    }

    /**
     * Removes the protocol, if it exists, from the front and
     * removes any random encoding characters
     * Extend this to do other url cleaning/pre-processing
     *
     * @param url - The String URL to normalize
     * @return normalizedUrl - The String URL that has no junk or surprises
     */
    public static String normalizeURL(String url) {
        // Decode URL to remove any %20 type stuff
        String normalizedUrl = url;
        try {
            // I've used a URLDecoder that's part of Java here,
            // but this functionality exists in most modern languages
            // and is universally called url decoding
            normalizedUrl = URLDecoder.decode(url, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
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
     * <p/>
     * Reference:
     * http://stackoverflow.com/questions/10046178/pattern-matching-for-url-classification
     *
     * @param url - Url to be tokenized
     * @return tokens - A String array of all the tokens
     */
    public static List<String> tokenizeURL(String url) {
        url = normalizeURL(url);
        // I assume that we're going to use the whole URL to find tokens in
        // If you want to just look in the GET parameters, or you want to ignore the domain
        // or you want to use the domain as a token itself, that would have to be
        // processed above the next line, and only the remaining parts split
        List<String> toReturn = new ArrayList<String>();

        // Split the URL by forward slashes. Most modern browsers will accept a URL
        // this malformed such as http://www.smashew.com/hello//how////are/you
        // hence the '+' in the regular expression.
        for (String part : url.split("/+"))
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