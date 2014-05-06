package org.apache.streams.urls;

import com.google.common.base.Preconditions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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

public class LinkCrawler extends LinkResolver
{
    private final static Logger LOGGER = LoggerFactory.getLogger(LinkCrawler.class);

    public LinkCrawler(String originalURL) {
        super(originalURL);
    }

    public void run() {

        Preconditions.checkNotNull(linkDetails);

        Preconditions.checkNotNull(linkDetails.getOriginalURL());

        super.run();

        Preconditions.checkNotNull(linkDetails.getFinalURL());

        crawlLink(linkDetails.getFinalURL());

    }


    public void crawlLink(String url)
    {

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

                if(linkDetails.getRedirectCount() > 0 && BOTS_ARE_OK.contains(thisURL.getHost()))
                    connection.addRequestProperty("Referrer", linkDetails.getOriginalURL());
            }

            connection.setReadTimeout(DEFAULT_HTTP_TIMEOUT);
            connection.setConnectTimeout(DEFAULT_HTTP_TIMEOUT);

            connection.setInstanceFollowRedirects(false);

            if(linkDetails.getCookies() != null)
                for (String cookie : linkDetails.getCookies())
                    connection.addRequestProperty("Cookie", cookie.split(";", 1)[0]);

            connection.connect();

            linkDetails.setFinalResponseCode((long)connection.getResponseCode());

            InputStreamReader in = new InputStreamReader((InputStream) connection.getContent());
            BufferedReader buff = new BufferedReader(in);
            StringBuffer contentBuffer = new StringBuffer();
            String line;
            do {
                line = buff.readLine();
                contentBuffer.append(line + "\n");
            } while (line != null);

            linkDetails.setContentType(connection.getContentType());
            linkDetails.setContent(contentBuffer.toString());

            /**************
             *
             */
            Map<String,List<String>> headers = createCaseInsensitiveMap(connection.getHeaderFields());
            /******************************************************************
             * If they want us to set cookies, well, then we will set cookies
             * Example URL:
             * http://nyti.ms/1bCpesx
             *****************************************************************/
            if(headers.containsKey(SET_COOKIE_IDENTIFIER))
                linkDetails.getCookies().add(headers.get(SET_COOKIE_IDENTIFIER).get(0));

            switch (linkDetails.getFinalResponseCode().intValue())
            {
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
                    if(!linkDetails.getOriginalURL().toLowerCase().equals(connection.getURL().toString().toLowerCase()))
                        linkDetails.setFinalURL(connection.getURL().toString());
                    if(!headers.containsKey(LOCATION_IDENTIFIER))
                    {
                        LOGGER.info("Headers: {}", headers);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.REDIRECT_ERROR);
                    }
                    else
                    {
                        linkDetails.setRedirected(Boolean.TRUE);
                        linkDetails.setRedirectCount(linkDetails.getRedirectCount().longValue()+1);
                        unwindLink(connection.getHeaderField(LOCATION_IDENTIFIER));
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
        }
        catch (MalformedURLException e)
        {
            // the URL is trash, so, it can't load it.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.MALFORMED_URL);
        }
        catch (IOException ex)
        {
            // there was an issue we are going to set to error.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
        }
        catch (Exception ex)
        {
            // there was an unknown issue we are going to set to exception.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.EXCEPTION);
        }
        finally
        {
            if (connection != null)
                connection.disconnect();
        }
    }

}