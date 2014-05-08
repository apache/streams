package org.apache.streams.urls;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TestDomainSensitivity {

    @Test
    public void testSimple() {

        DomainSensitivity.purgeAllDomainWaitTimes();
        String domain1 = "smashew.com";

        // safe to run...
        assertEquals("smashew.com: No need to wait", 0, DomainSensitivity.waitTimeForDomain(domain1));
        // get required sleep
        long smashewSleepTime1 = DomainSensitivity.waitTimeForDomain(domain1);
        // sleep
        System.out.println("Sleeping: " + new Date().getTime() + "-" + smashewSleepTime1);
        safeSleep(smashewSleepTime1);
        System.out.println("Slept For: " + new Date().getTime() + "-" + smashewSleepTime1);
        // safe to run again
        assertEquals("smashew.com: No need to wait", 0, DomainSensitivity.waitTimeForDomain(domain1));
    }

    private static void safeSleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(Exception e) {
            // noOp
        }
    }

    @Test
    public void testSingle() {

        DomainSensitivity.purgeAllDomainWaitTimes();

        String domain1 = "smashew.com";
        String domain2 = "google.com";

        long smashewSleepTime1 = DomainSensitivity.waitTimeForDomain(domain1);
        long smashewSleepTime2 = DomainSensitivity.waitTimeForDomain(domain1);
        long smashewSleepTime3 = DomainSensitivity.waitTimeForDomain(domain1);
        long smashewSleepTime4 = DomainSensitivity.waitTimeForDomain(domain1);

        System.out.println("smashew.com: " + smashewSleepTime1 + "," + smashewSleepTime2 + "," + smashewSleepTime3  + "," + smashewSleepTime4);

        assertEquals("smashew.com: No need to wait", 0, smashewSleepTime1);
        assertTrue("smashew.com: Wait for at least min x 1", smashewSleepTime2 >= (DomainSensitivity.RECENT_DOMAINS_BACKOFF - DomainSensitivity.DEFAULT_STAGGER));
        assertTrue("smashew.com: Wait for at least min x 2", smashewSleepTime3 >= (DomainSensitivity.RECENT_DOMAINS_BACKOFF * 2) - (DomainSensitivity.DEFAULT_STAGGER * 2));
        assertTrue("smashew.com: Wait for at least min x 3", smashewSleepTime4 >= (DomainSensitivity.RECENT_DOMAINS_BACKOFF * 3) - (DomainSensitivity.DEFAULT_STAGGER * 3));

        long timeBeforeSleep = new Date().getTime();
        System.out.println("Sleeping for: " + smashewSleepTime4 + " ms");

        safeSleep(smashewSleepTime4);
        System.out.println("Actually slept for: " + (new Date().getTime() - timeBeforeSleep) + " ms");

        long postSleepDomain1 = DomainSensitivity.waitTimeForDomain(domain1);
        System.out.println("smashew.com: Post Sleep domain1: " + postSleepDomain1);
        assertEquals("Smashew.com: No need to wait after sleep", 0, postSleepDomain1);

    }

    @Test
    public void testMulti() {

        DomainSensitivity.purgeAllDomainWaitTimes();
        String domain1 = "smashew.com";
        String domain2 = "google.com";

        long smashewSleepTime1 = DomainSensitivity.waitTimeForDomain(domain1);
        long smashewSleepTime2 = DomainSensitivity.waitTimeForDomain(domain1);
        long smashewSleepTime3 = DomainSensitivity.waitTimeForDomain(domain1);

        long googleSleepTime1 = DomainSensitivity.waitTimeForDomain(domain2);
        long googleSleepTime2 = DomainSensitivity.waitTimeForDomain(domain2);

        System.out.println("smashew.com: " + smashewSleepTime1 + "," + smashewSleepTime2 + "," + smashewSleepTime3);
        System.out.println("google.com: " + googleSleepTime1 + "," + googleSleepTime2);

        assertEquals("smashew.com: No need to wait", 0, smashewSleepTime1);
        assertTrue("smashew.com: Wait for at least min x 1", smashewSleepTime2 >= (DomainSensitivity.RECENT_DOMAINS_BACKOFF - DomainSensitivity.DEFAULT_STAGGER));
        assertTrue("smashew.com: Wait for at least min x 2", smashewSleepTime3 >= (DomainSensitivity.RECENT_DOMAINS_BACKOFF * 2) - (DomainSensitivity.DEFAULT_STAGGER * 2));

        assertEquals("google.com: No need to wait", 0, googleSleepTime1);
        assertTrue("google.com: No need to wait", googleSleepTime2 >= DomainSensitivity.RECENT_DOMAINS_BACKOFF - DomainSensitivity.DEFAULT_STAGGER);

        try {
            System.out.println("WAITING FOR: " + smashewSleepTime3);
            Thread.sleep(smashewSleepTime3);
        }
        catch(Exception e) {
            // noOp
        }

        long postSleepDomain1 = DomainSensitivity.waitTimeForDomain(domain1);
        long postSleepDomain2 = DomainSensitivity.waitTimeForDomain(domain2);

        System.out.println("smashew.com: Post Sleep domain1: " + postSleepDomain1);
        System.out.println("google.com:  Post Sleep domain2: " + postSleepDomain2);

        assertEquals("Smashew.com: No need to wait after sleep", 0, postSleepDomain1);
        assertEquals("google.com: No need to wait after sleep", 0, postSleepDomain2);

    }

}
