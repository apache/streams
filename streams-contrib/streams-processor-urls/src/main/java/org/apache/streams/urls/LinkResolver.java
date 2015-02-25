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
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

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

    // This element holds all the information about all the re-directs that have taken place
    // and the steps and HTTP codes that occurred inside of each step.
    private final LinkDetails linkDetails;

    /**
     * Get the link details
     *
     * @return Detailed log of every redirection that took place with the browser along with it it's ultimate status code.
     */
    public LinkDetails getLinkDetails() {
        return linkDetails;
    }

    private static final int BYTE_BUFFER_SIZE = 1024 * 50;
    private static final int MAX_ALLOWABLE_DOWNLOAD = 1024 * 1024 * 10;


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

        try {
            if(LinkResolverHelperFunctions.isURL(linkDetails.getOriginalURL())) {
                // we are going to try three times just in case we catch a slow server or one that needs
                // to be warmed up. This tends to happen many times with smaller private servers
                for (int i = 0; (i < 3) && linkDetails.getFinalURL() == null; i++)
                    if (linkDetails.getLinkStatus() != LinkDetails.LinkStatus.SUCCESS) {
                        this.getLinkDetails().getRedirects().clear();
                        unwindLink(linkDetails.getOriginalURL());
                    }

                if (linkDetails.getFinalURL() != null && StringUtils.isNotEmpty(linkDetails.getFinalURL())) {
                    linkDetails.setFinalURL(LinkResolverHelperFunctions.getCanonicalURL(linkDetails.getFinalURL()));
                    linkDetails.setNormalizedURL(LinkResolverHelperFunctions.getCanonicalURL(linkDetails.getFinalURL()));
                    linkDetails.setUrlParts(tokenizeURL(linkDetails.getNormalizedURL()));
                }
            }
            else {
                // This is a short circuited path. If the link is deemed to not be a valid URL
                // we set the status of a malformed URL and return.
                linkDetails.setLinkStatus(LinkDetails.LinkStatus.MALFORMED_URL);
            }
        } catch (Throwable e) {
            // we want everything to be processed, because each datum should get a
            // response in this case
            // there was an unknown issue we are going to set to exception.
            LOGGER.warn("Unexpected Exception: {}", e.getMessage());
            e.printStackTrace();
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.EXCEPTION);
        }


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
        if ((linkDetails.getRedirects().size() > 0 && (linkDetails.getOriginalURL().equals(url) || linkDetails.getRedirects().contains(url)))
                || (linkDetails.getRedirects().size() > MAX_ALLOWED_REDIRECTS))
        {
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.LOOP);
            return;
        }

        if (!linkDetails.getOriginalURL().equals(url))
            linkDetails.getRedirects().add(url);

        URLConnection urlConnection = null;
        HttpURLConnection connection = null;

        // Store where the redirected link will go (if there is one)
        String reDirectedLink = null;

        try {
            urlConnection = LinkResolverHelperFunctions.constructHTTPConnection(url, this.linkDetails);

            if(!(urlConnection instanceof HttpURLConnection)) {
                linkDetails.setFinalResponseCode((long)200);
                linkDetails.setFinalURL(urlConnection.getURL().toString());
                linkDetails.setDomain(new URL(linkDetails.getFinalURL()).getHost());
                linkDetails.setContentType(urlConnection.getContentType().contains(";") ? urlConnection.getContentType().split(";")[0] : urlConnection.getContentType());
                setContent(urlConnection);
            }
            else {
                connection = (HttpURLConnection)urlConnection;
                linkDetails.setFinalResponseCode((long) connection.getResponseCode());

                Map<String, List<String>> headers = createCaseInsensitiveMap(connection.getHeaderFields());
                /******************************************************************
                 * If they want us to set cookies, well, then we will set cookies
                 * Example URL:
                 * http://nyti.ms/1bCpesx
                 *****************************************************************/
                if (headers.containsKey(LinkResolverHelperFunctions.SET_COOKIE_IDENTIFIER)) {
                    linkDetails.getCookies().add(headers.get(LinkResolverHelperFunctions.SET_COOKIE_IDENTIFIER).get(0));
                }

                switch (linkDetails.getFinalResponseCode().intValue()) {
                    /**
                     * W3C HTTP Response Codes:
                     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
                     */
                    case 200: // HTTP OK
                        linkDetails.setFinalURL(connection.getURL().toString());
                        linkDetails.setDomain(new URL(linkDetails.getFinalURL()).getHost());
                        linkDetails.setContentType(connection.getContentType().contains(";") ? connection.getContentType().split(";")[0] : connection.getContentType());
                        setContent(connection);
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
                        if (!headers.containsKey(LinkResolverHelperFunctions.LOCATION_IDENTIFIER)) {
                            LOGGER.warn("Redirection Error: {}", headers);
                            linkDetails.setLinkStatus(LinkDetails.LinkStatus.REDIRECT_ERROR);
                        } else {
                            reDirectedLink = connection.getHeaderField(LinkResolverHelperFunctions.LOCATION_IDENTIFIER);
                        }

                        // This fixes malformed URLs by allowing re-directs to relative paths
                        if(!LinkResolverHelperFunctions.isURL(reDirectedLink)) {
                            try {
                                reDirectedLink = new URL(new URL(url), reDirectedLink).toString();
                            }
                            catch(MalformedURLException e) {
                                LOGGER.info("Unable to concat the URLs together: {} - {}", url, reDirectedLink);
                                throw e;
                            }
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
                        LOGGER.warn("Unrecognized HTTP Response Code: {}", linkDetails.getFinalResponseCode());
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.NOT_FOUND);
                        break;
                }
            }
        } catch (MalformedURLException e) {
            // the URL is trash, so, it can't load it.
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.MALFORMED_URL);
        } catch (SocketTimeoutException e) {
            LOGGER.warn("Socket Timeout: {}", url);
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.TIME_OUT);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } catch (IOException e) {
            LOGGER.warn("Socket Timeout: {} - {}", e, url);
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } catch(Throwable e) {
            LOGGER.warn("Unexpected Runtime Exception: {} - {}", e, url);
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
            linkDetails.setRawContent(StringUtils.EMPTY);
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

    private void setContent(URLConnection connection) {
        InputStream inputStream = null;
        try {
            if (linkDetails.getContentType() != null) {
                inputStream = connection.getInputStream();

                if (inputStream == null) {
                    linkDetails.setLinkStatus(LinkDetails.LinkStatus.EMPTY);
                    linkDetails.setRawContent(StringUtils.EMPTY);
                    return;
                }

                if (linkDetails.getContentType().equalsIgnoreCase("text/html")) {
                    linkDetails.setContentsType(LinkDetails.ContentsType.RAW_HTML);
                    String rawHTML = convertInputStream(inputStream, connection.getContentEncoding() == null ? "UTF-8" : connection.getContentEncoding().toUpperCase());

                    // we were unable to get the raw information
                    if (rawHTML == null || rawHTML.equals(StringUtils.EMPTY)) {
                        linkDetails.setRawContent(StringUtils.EMPTY);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
                    } else {
                        linkDetails.setRawContent(rawHTML);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.SUCCESS);
                    }
                } else {
                    linkDetails.setContentsType(LinkDetails.ContentsType.BASE_64);
                    String base64 = convertInputStreamToBase64(inputStream);
                    if (base64 == null || base64.equals(StringUtils.EMPTY)) {
                        linkDetails.setRawContent(StringUtils.EMPTY);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
                    } else {
                        linkDetails.setRawContent(base64);
                        linkDetails.setLinkStatus(LinkDetails.LinkStatus.SUCCESS);
                    }
                }
            }
        } catch (LinkResolverContentTooLargeException e) {
            LOGGER.warn("Too Large: {}", connection.getURL().toString());
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.TOO_LARGE);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } catch (SocketTimeoutException e) {
            LOGGER.warn("Socket Timeout: {}", connection.getURL().toString());
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.TIME_OUT);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } catch (IOException e) {
            LOGGER.warn("Socket Timeout: {} - {}", e, connection.getURL().toString());
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } catch(Throwable e)  {
            LOGGER.warn("Unknown Error: {} - {}", e, connection.getURL().toString());
            linkDetails.setLinkStatus(LinkDetails.LinkStatus.ERROR);
            linkDetails.setRawContent(StringUtils.EMPTY);
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                LOGGER.warn("Couldn't close the InputStream: {}", e);
            }
        }
    }

    private String convertInputStreamToBase64(final InputStream in) throws IOException, LinkResolverContentTooLargeException {
        return new BASE64Encoder().encode(getByteArrayOutputStream(in).toByteArray());
    }

    private String convertInputStream(final InputStream in, String encoding) throws IOException, LinkResolverContentTooLargeException{
        ByteArrayOutputStream byteArrayOutputStream = encoding.equalsIgnoreCase("gzip") ?
                getByteArrayOutputStream(new GZIPInputStream(in)) :
                getByteArrayOutputStream(in);

        encoding = encoding.equalsIgnoreCase("gzip") ?
                "UTF-8" :
                encoding;

        String beforeConvert = new String(byteArrayOutputStream.toByteArray(), encoding);
        return new String(beforeConvert.getBytes(), "UTF-8");
    }

    private ByteArrayOutputStream getByteArrayOutputStream(InputStream in) throws IOException, LinkResolverContentTooLargeException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[BYTE_BUFFER_SIZE];
        int totalRead = 0;
        int rd;
        while ((rd = in.read(buf, 0, BYTE_BUFFER_SIZE)) >= 0) {
            totalRead += rd;
            if (totalRead > MAX_ALLOWABLE_DOWNLOAD)
                throw new LinkResolverContentTooLargeException();
            out.write(buf, 0, rd);
        }
        return out;
    }

    private Map<String, List<String>> createCaseInsensitiveMap(Map<String, List<String>> input) {
        Map<String, List<String>> toReturn = new HashMap<String, List<String>>();
        for (String k : input.keySet())
            if (k != null && input.get(k) != null)
                toReturn.put(k.toLowerCase(), input.get(k));
        return toReturn;
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
        // I assume that we're going to use the whole URL to find tokens in
        // If you want to just look in the GET parameters, or you want to ignore the domain
        // or you want to use the domain as a token itself, that would have to be
        // processed above the next line, and only the remaining parts split
        List<String> toReturn = new ArrayList<>();

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