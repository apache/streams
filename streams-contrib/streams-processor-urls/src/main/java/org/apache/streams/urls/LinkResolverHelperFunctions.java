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

    private static final String REGEX_URL =
            "(?:(?:https?|ftp)://)" +                                               // protocol identifier
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

    public static long waitTimeForDomain(String domain) {
        domain = domain.toLowerCase();

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
