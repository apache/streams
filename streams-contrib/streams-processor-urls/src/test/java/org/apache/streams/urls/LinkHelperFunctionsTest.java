package org.apache.streams.urls;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.*;

public class LinkHelperFunctionsTest {

    @Test
    public void testIsURL() {
        assertTrue(LinkResolverHelperFunctions.isURL("http://goo.gl/wSrHDA"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://ow.ly/u4Kte"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://x.co/3yapt"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://bit.ly/1cX5Rh4"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://t.co/oP8JYB0MYW"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://goo.gl/wSrHDA"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://t.co/fBoCby3l1t"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://paper.li/GuyKawasaki"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://www.google.com"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://goo.gl/wSrHDA"));
        assertTrue(LinkResolverHelperFunctions.isURL("http://www.cnn.com"));
    }

    @Test
    public void testContainsURL() {
        assertTrue(LinkResolverHelperFunctions.containsURLs("here is the URL: http://goo.gl/wSrHDA"));
        assertTrue(LinkResolverHelperFunctions.containsURLs("a lovely day for URLing it up http://ow.ly/u4Kte"));
        assertTrue(LinkResolverHelperFunctions.containsURLs("http://x.co/3yapt is really cool"));
        assertTrue(LinkResolverHelperFunctions.containsURLs("http://bit.ly/1cX5Rh4 me likes"));
        assertTrue(LinkResolverHelperFunctions.containsURLs("http://t.co/oP8JYB0MYW wtf mate?"));
        assertTrue(LinkResolverHelperFunctions.containsURLs("Every morning is a good morning in URL world: http://goo.gl/wSrHDA"));

        assertFalse(LinkResolverHelperFunctions.containsURLs("Every day I awake, only to find, I have no URLS"));
        assertFalse(LinkResolverHelperFunctions.containsURLs("Http:// to be or not to be"));
        assertFalse(LinkResolverHelperFunctions.containsURLs("Can I get an http://X up on the board pat?"));
        assertFalse(LinkResolverHelperFunctions.containsURLs("You must remember Joey, no matter how much you ftp://stink you must never, EVER, take a shower in my dressing room!"));
    }

    @Test
    public void testReallyLongURL() {
        assertTrue(LinkResolverHelperFunctions.isURL("http://map.sysomos.com/?q=Pantene%20AND%20%28shampoo%20OR%20conditioner%20OR%20style%20OR%20styling%20OR%20styled%20OR%20treatment%20OR%20treatments%20OR%20hair%20OR%20therapy%20OR%20%22Always%20Smooth%22%20OR%20%22Anti%20Dandruff%22%20OR%20antidandruff%20OR%20anti-dandruff%20OR%20%22anti%20dandruff%22%20OR%20%22Aqua%20Light%22%20OR%20care%20OR%20caring%20OR%20%22Classic%20Care%22%20OR%20%22Colour%20Therapy%22%20OR%20%22Daily%20Moisture%20Renewal%22%20OR%20%22Deep%20Fortifying%22%20OR%20%22Hydrating%20Curls%22%20OR%20%22Nature%20Fusion%22%20OR%20%22Nourished%20Shine%22%20OR%20%22Strengthening%22%20OR%20%22Always%20Smooth%22%20OR%20%223%20Minute%20Miracle%22%20OR%20%22All%20Day%20Smooth%20Lightweight%20Creme%22%20OR%20moisture%20OR%20%22Pro-Vitamin%22%20OR%20%22Pro-V%22%20OR%20%22silky%20hair%22%20OR%20%22rinse-off%22%20OR%20%22nourishing%20treatment%22%20OR%20%22UV%20filter%22%20OR%20%22leave%20in%20formula%22%20OR%20%22Intensive%20treatment%22%20OR%20%22Hair%20Moisturiser%22%20OR%20%22Leave%20In%20Spray%20Conditioner%22%20OR%20%22heat%20styling%22%20OR%20%22Deep%20Fortifying%20Treatment%22%20OR%20%22Intensive%20Damage%20Repair%20Oil%22%20OR%20%22split%20ends%22%20OR%20%22Night%20Miracle%22%20OR%20%22cr%C3%A8me%20serum%22%29%20AND%20NOT%20%28%22limit%201%22%20OR%20%22limit%20one%22%20OR%20preview%20OR%20previews%20OR%20freebie%20OR%20freebies%20OR%20%23freebie%20OR%20discount%20OR%20discounts%20OR%20discounted%20OR%20%22free%20sample%22%20OR%20%22free%20samples%22%20OR%20sample%20OR%20samples%20OR%20deal%20OR%20deals%20OR%20%22deal%20alert%22%20OR%20circular%20OR%20coupon%20OR%20coupons%20OR%20couponing%20OR%20voucher%20OR%20vouchers%20OR%20bogo%20OR%20%22buy%20one%20get%20one%22%20OR%20bogof%20OR%20%22special%20offers%22%20OR%20%22special%20offer%22%20OR%20%22buy%20cheap%22%20OR%20expire%20OR%20expires%20OR%20expired%20OR%20%22special%20price%22%20OR%20%22limited%20time%20offer%22%20OR%20%22while%20supplies%20last%22%20OR%20%22while%20stocks%20last%22%20OR%20free%20OR%20%22Free%20delivery%22%20OR%20offer%20OR%20offers%20OR%20%22half%20price%22%20OR%20%22halfprice%22%20OR%20reduced%20OR%20onsale%20OR%20%22per%20pack%22%20OR%20%22buy%20cheap%22%20OR%20%22buy%20new%22%20OR%20%22price%20crunch%22%20OR%20%22blog%20sale%22%20OR%20%22blog%20sales%22%20OR%20%22limited%20time%22%20OR%20prize%20OR%20%22win%20a%22%20OR%20%22free%20sample%22%20OR%20%22year%27s%20supply%22%20OR%20%22years%20supply%22%20OR%20competition%20OR%20competitions%20OR%20prize%20OR%20prizes%20OR%20giveaway%20OR%20%22buy%20one%20get%20one%20free%22%20OR%20%22buy%201%20get%201%20free%22%20OR%20%22buy%20one%20get%20two%20free%22%20OR%20%22buy%20one%20get%20one%20half%20price%22%20OR%20%222%20for%201%22%20OR%20%22two%20for%20one%22%20OR%20%222%204%201%22%20OR%20241%20OR%20%223%20for%202%22%20OR%203for2%20OR%20%22mix%20and%20match%22%20OR%20%40offeroasis%20OR%20%40groupon%20OR%20groupon%20OR%20%22%23hotukdeals%22%20OR%20%22%23hukd%22%20OR%20%22Free%20bottle%22%20OR%20%22Homebargain%22%20OR%20%22value%20pack%22%20OR%20%22Special%20pack%22%29&luc=true&sDy=2013-07-01&eDy=2014-05-31&mGo=AU,NZ&viw=dashboard_overall_content"));
    }


    @Test
    public void testSimple() {

        LinkResolverHelperFunctions.purgeAllDomainWaitTimes();
        String domain1 = "smashew.com";

        // safe to run...
        assertEquals("smashew.com: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain(domain1));
        // get required sleep
        long smashewSleepTime1 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        // sleep
        System.out.println("Sleeping: " + new Date().getTime() + "-" + smashewSleepTime1);
        safeSleep(smashewSleepTime1);
        System.out.println("Slept For: " + new Date().getTime() + "-" + smashewSleepTime1);
        // safe to run again
        assertEquals("smashew.com: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain(domain1));
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

        LinkResolverHelperFunctions.purgeAllDomainWaitTimes();

        String domain1 = "smashew.com";
        String domain2 = "google.com";

        long smashewSleepTime1 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long smashewSleepTime2 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long smashewSleepTime3 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long smashewSleepTime4 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);

        System.out.println("smashew.com: " + smashewSleepTime1 + "," + smashewSleepTime2 + "," + smashewSleepTime3  + "," + smashewSleepTime4);

        assertEquals("smashew.com: No need to wait", 0, smashewSleepTime1);
        assertTrue("smashew.com: Wait for at least min x 1", smashewSleepTime2 >= (LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF - LinkResolverHelperFunctions.DEFAULT_STAGGER));
        assertTrue("smashew.com: Wait for at least min x 2", smashewSleepTime3 >= (LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF * 2) - (LinkResolverHelperFunctions.DEFAULT_STAGGER * 2));
        assertTrue("smashew.com: Wait for at least min x 3", smashewSleepTime4 >= (LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF * 3) - (LinkResolverHelperFunctions.DEFAULT_STAGGER * 3));

        long timeBeforeSleep = new Date().getTime();
        System.out.println("Sleeping for: " + smashewSleepTime4 + " ms");

        safeSleep(smashewSleepTime4);
        System.out.println("Actually slept for: " + (new Date().getTime() - timeBeforeSleep) + " ms");

        long postSleepDomain1 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        System.out.println("smashew.com: Post Sleep domain1: " + postSleepDomain1);
        assertEquals("Smashew.com: No need to wait after sleep", 0, postSleepDomain1);

    }

    @Test
    public void testUnprotectedDomain() {
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.t.co"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.bit.ly"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("www.bit.ly"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("bit.ly"));
        assertEquals("t.co: No need to wait", 0, LinkResolverHelperFunctions.waitTimeForDomain("bit.ly"));
    }

    @Test
    public void testMulti() {

        LinkResolverHelperFunctions.purgeAllDomainWaitTimes();
        String domain1 = "smashew.com";
        String domain2 = "google.com";

        long smashewSleepTime1 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long smashewSleepTime2 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long smashewSleepTime3 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);

        long googleSleepTime1 = LinkResolverHelperFunctions.waitTimeForDomain(domain2);
        long googleSleepTime2 = LinkResolverHelperFunctions.waitTimeForDomain(domain2);

        System.out.println("smashew.com: " + smashewSleepTime1 + "," + smashewSleepTime2 + "," + smashewSleepTime3);
        System.out.println("google.com: " + googleSleepTime1 + "," + googleSleepTime2);

        assertEquals("smashew.com: No need to wait", 0, smashewSleepTime1);
        assertTrue("smashew.com: Wait for at least min x 1", smashewSleepTime2 >= (LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF - LinkResolverHelperFunctions.DEFAULT_STAGGER));
        assertTrue("smashew.com: Wait for at least min x 2", smashewSleepTime3 >= (LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF * 2) - (LinkResolverHelperFunctions.DEFAULT_STAGGER * 2));

        assertEquals("google.com: No need to wait", 0, googleSleepTime1);
        assertTrue("google.com: No need to wait", googleSleepTime2 >= LinkResolverHelperFunctions.RECENT_DOMAINS_BACKOFF - LinkResolverHelperFunctions.DEFAULT_STAGGER);

        try {
            System.out.println("WAITING FOR: " + smashewSleepTime3);
            Thread.sleep(smashewSleepTime3);
        }
        catch(Exception e) {
            // noOp
        }

        long postSleepDomain1 = LinkResolverHelperFunctions.waitTimeForDomain(domain1);
        long postSleepDomain2 = LinkResolverHelperFunctions.waitTimeForDomain(domain2);

        System.out.println("smashew.com: Post Sleep domain1: " + postSleepDomain1);
        System.out.println("google.com:  Post Sleep domain2: " + postSleepDomain2);

        assertEquals("Smashew.com: No need to wait after sleep", 0, postSleepDomain1);
        assertEquals("google.com: No need to wait after sleep", 0, postSleepDomain2);

    }

}
