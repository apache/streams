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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * This is a static utility helper class to verify strings are URLs,
 * obey domain sensitivity, and find URLs within a string.
 * It cannot be, instantiated and can only be referenced through
 * the static accessor functions
 *
 */
public final class LinkResolverHelperFunctions {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkResolverHelperFunctions.class);

    private static final String REGEX_URL =
            "(?:(?:https?|ftp)://)" +                                                       // protocol identifier
                    "(?:\\S+(?::\\S*)?@)?" +                                                // user:pass authentication
                    "(?:" +
                    "(?!(?:10|127)(?:\\.\\d{1,3}){3})" +                                    // IP address exclusion
                    "(?!(?:169\\.254|192\\.168)(?:\\.\\d{1,3}){2})" +                       // private & local networks
                    "(?!172\\.(?:1[6-9]|2\\d|3[0-1])(?:\\.\\d{1,3}){2})" +
                    "(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])" +                              // IP address dotted notation octets
                    "(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}" +                          // excludes loop-back network 0.0.0.0, excludes network & broadcast addresses
                    "(?:\\.(?:[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))" +                      // excludes reserved space >= 224.0.0.0, (first & last IP address of each class)
                    "|" +
                    "(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)" +         // host name
                    "(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*" +     // domain name
                    "(?:\\.(?:[a-z\\u00a1-\\uffff]{2,}))" +                                 // TLD identifier
                    ")" +
                    "(?::\\d{2,5})?" +                                                      // port number
                    "(?:/[^\\s]*)?";                                                        // resource path

    private static final String REGEX_URL_EXPLICIT = "^" + REGEX_URL + "$";

    // The amount of time we want to space between domain calls
    public static final long RECENT_DOMAINS_BACKOFF = 1000;
    public static final long DEFAULT_STAGGER = RECENT_DOMAINS_BACKOFF / 10;

    public static final int DEFAULT_HTTP_TIMEOUT = 10000;              // We will only wait a max of 10,000 milliseconds (10 seconds) for any HTTP response
    public static final String LOCATION_IDENTIFIER = "location";
    public static final String SET_COOKIE_IDENTIFIER = "set-cookie";

    private static final Set<String> UNPROTECTED_DOMAINS = new HashSet<String>() {{
        add("t.co");        // Twitter link shortener
        add("bit.ly");      // Bitly link shortener
        add("x.co");        // x.co link shortener
        add("goo.gl");      // Google's link shortener
        add("ow.ly");       // Owly's link shortener
    }};

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

    // Map to store the information of recent domains, with the last time they were accessed.
    private static final ConcurrentMap<String, Date> RECENT_DOMAINS = new ConcurrentHashMap<String, Date>();

    private static Timer timer;

    /**
     * Check to see if this string is a URL or not
     * @param possibleURL
     * The possible URL that we would like to test
     * @return
     * Whether or not it is a URL
     */
    public static boolean isURL(String possibleURL) {
        return possibleURL.matches(REGEX_URL_EXPLICIT);
    }

    public static boolean containsURLs(String possiblyHasURLs) {
        return possiblyHasURLs != null && Pattern.compile(REGEX_URL).matcher(possiblyHasURLs).find();
    }

    private LinkResolverHelperFunctions() {
        // force it not to be instantiated.
    }

    public static void purgeAllDomainWaitTimes() {
        RECENT_DOMAINS.clear();
    }

    public static HttpURLConnection constructHTTPConnection(LinkDetails linkDetails) throws IOException {
        return constructHTTPConnection(linkDetails.getFinalURL(), linkDetails);
    }

    public static HttpURLConnection constructHTTPConnection(String url, LinkDetails linkDetails) throws IOException {
        // Turn the string into a URL
        URL thisURL = new URL(url);

        // Be sensitive to overloading domains STREAMS-77
        try {
            long domainWait = LinkResolverHelperFunctions.waitTimeForDomain(thisURL.getHost());
            if (domainWait > 0) {
                LOGGER.debug("Waiting for domain: {}", domainWait);
                Thread.sleep(domainWait);
            }
        } catch(Exception e) {
            // noOp
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

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
            if(linkDetails.getRedirects().size() > 0)
                connection.addRequestProperty("Referrer", linkDetails.getRedirects().get(linkDetails.getRedirects().size() - 1));
        }

        connection.setReadTimeout(DEFAULT_HTTP_TIMEOUT);
        connection.setConnectTimeout(DEFAULT_HTTP_TIMEOUT);

        // we want to follow this behavior on our own to ensure that we are getting to the
        // proper place. This is especially true with links that are wounded by special
        // link winders
        connection.setInstanceFollowRedirects(false);

        if (linkDetails.getCookies() != null)
            for (String cookie : linkDetails.getCookies())
                connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);

        connection.connect();

        return connection;
    }


    public static long waitTimeForDomain(String domain) {
        domain = domain.toLowerCase();

        // if they are in an unprotected domain, then return
        if(UNPROTECTED_DOMAINS.contains(domain.startsWith("www.") ? domain.replaceFirst("www\\.", "") : domain))
            return 0;

        long toReturn = 0;
        synchronized (LinkResolverHelperFunctions.class) {
            purgeAnyExpiredDomains();
            // if the timer doesn't exist, then setup the timer (IE: first time called)
            if(timer == null)
                setupTimer();
            long currentTime = new Date().getTime();

            if(RECENT_DOMAINS.containsKey(domain)) {
                // find the time it wants us to wait until
                long nextTime = RECENT_DOMAINS.get(domain).getTime();
                long random = (long)((Math.random() * (RECENT_DOMAINS_BACKOFF / 5))); // stagger

                // back-off has been satisfied
                if(currentTime >= nextTime)
                    RECENT_DOMAINS.put(domain, new Date(currentTime + RECENT_DOMAINS_BACKOFF));
                else {
                    // we are going to have to wait longer than the back-off
                    // add the time we told them they needed to wait
                    toReturn = (nextTime - currentTime) + RECENT_DOMAINS_BACKOFF;
                    RECENT_DOMAINS.put(domain, new Date(currentTime + toReturn));
                    toReturn += random + 1;
                }
            } else {
                // no wait
                RECENT_DOMAINS.put(domain, new Date(currentTime + RECENT_DOMAINS_BACKOFF));
            }
        } // end synchronized block

        return toReturn;
    }

    /**
     * Quick function to setup the daemon to clear domains to keep our memory foot-print low
     */
    private static void setupTimer() {
        timer = new Timer(true);
        timer.schedule(new TimerTask() {
            public void run() {
                purgeAnyExpiredDomains();
            }
        }, RECENT_DOMAINS_BACKOFF * 2);
    }

    /**
     * called by the timer to expire any domains
     */
    private static void purgeAnyExpiredDomains() {
        // ensure this method is synchronized to get the proper information
        synchronized (LinkResolverHelperFunctions.class) {
            // figure the time that we would like for these domains to expire
            long currentTime = new Date().getTime();
            // see if there is any work that 'can' be done
            if(RECENT_DOMAINS.size() != 0) {
                // create a temporary list of the items that can be removed
                Collection<String> ableToRemove = new HashSet<String>();


                // iterate through all the domains (keys)
                // if it qualifies, we can add it to the remove list
                for(String k : RECENT_DOMAINS.keySet())
                    if(currentTime >= RECENT_DOMAINS.get(k).getTime())
                        ableToRemove.add(k);

                if(ableToRemove.size() > 0)         // if there are domains to remove, then remove them
                    for(String k : ableToRemove) // iterate through every domain that we can remove
                        RECENT_DOMAINS.remove(k);   // remove the domain from our map.
            }
        }
    }

}
