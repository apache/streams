package org.apache.streams.urls;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This is a static utility helper class to obey domain sensitivity. It cannot be
 * instantiated and can only be referenced through the static accessor functions
 *
 */
public abstract class DomainSensitivity {

    // The amount of time we want to space between domain calls
    public static final long RECENT_DOMAINS_BACKOFF = 1000;
    public static final long DEFAULT_STAGGER = RECENT_DOMAINS_BACKOFF / 10;

    // Map to store the information of recent domains, with the last time they were accessed.
    private static final ConcurrentMap<String, Date> RECENT_DOMAINS = new ConcurrentHashMap<String, Date>();

    private static Timer timer;

    private DomainSensitivity() {
        // force it not to be instantiated.
    }

    public static void purgeAllDomainWaitTimes() {
        RECENT_DOMAINS.clear();
    }

    public static long waitTimeForDomain(String domain) {
        domain = domain.toLowerCase();

        long toReturn = 0;
        synchronized (DomainSensitivity.class) {
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
        synchronized (RECENT_DOMAINS) {
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
