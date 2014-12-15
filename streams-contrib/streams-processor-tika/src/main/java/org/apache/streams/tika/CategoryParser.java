package org.apache.streams.tika;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

public class CategoryParser implements Serializable
{
    /**
     * This method takes a URL and from that text alone determines what categories that URL belongs in.
     * @param url - String URL to categorize
     * @return categories - A List&lt;String&rt; of categories the URL seemingly belongs in
     */
    public static List<String> getCategoriesFromUrl(String url) {

        // Clean the URL to remove useless bits and encoding artifacts
        String normalizedUrl = normalizeURL(url);

        // Break the urls apart and get the good stuff
        String[] keywords = tokenizeURL(normalizedUrl);

        return null;
    }

    /**
     * Removes the protocol, if it exists, from the front and
     * removes any random encoding characters
     * Extend this to do other urls cleaning/pre-processing
     * @param url - The String URL to normalize
     * @return normalizedUrl - The String URL that has no junk or surprises
     */
    private static String normalizeURL(String url)
    {
        // Decode URL to remove any %20 type stuff
        String normalizedUrl = url;
        try {
            // I've used a URLDecoder that's part of Java here,
            // but this functionality exists in most modern languages
            // and is universally called urls decoding
            normalizedUrl = URLDecoder.decode(url, "UTF-8");
        }
        catch(UnsupportedEncodingException uee)
        {
            System.err.println("Unable to Decode URL. Decoding skipped.");
            uee.printStackTrace();
        }

        // Remove the protocol, http:// ftp:// or similar from the front
        if (normalizedUrl.contains("://"))
            normalizedUrl = normalizedUrl.split(":\\/\\/")[1];

        // Room here to do more pre-processing

        return normalizedUrl;
    }

    /**
     * Takes apart the urls into the pieces that make at least some sense
     * This doesn't guarantee that each token is a potentially valid keyword, however
     * because that would require actually iterating over them again, which might be
     * seen as a waste.
     * @param url - Url to be tokenized
     * @return tokens - A String array of all the tokens
     */
    private static String[] tokenizeURL(String url)
    {
        // I assume that we're going to use the whole URL to find tokens in
        // If you want to just look in the GET parameters, or you want to ignore the domain
        // or you want to use the domain as a token itself, that would have to be
        // processed above the next line, and only the remaining parts split
        String[] tokens = url.split("\\b|_");

        // One could alternatively use a more complex regex to remove more invalid matches
        // but this is subject to your (?:in)?ability to actually write the regex you want

        // These next two get rid of tokens that are too short, also.

        // Destroys anything that's not alphanumeric and things that are
        // alphanumeric but only 1 character long
        //String[] tokens = urls.split("(?:[\\W_]+\\w)*[\\W_]+");

        // Destroys anything that's not alphanumeric and things that are
        // alphanumeric but only 1 or 2 characters long
        //String[] tokens = urls.split("(?:[\\W_]+\\w{1,2})*[\\W_]+");

        return tokens;
    }

    // How this would be used
    public static void main(String[] args)
    {
        List<String> soQuestionUrlClassifications = getCategoriesFromUrl("http://stackoverflow.com/questions/10046178/pattern-matching-for-urls-classification");
        List<String> googleQueryURLClassifications = getCategoriesFromUrl("https://www.google.com/search?sugexp=chrome,mod=18&sourceid=chrome&ie=UTF-8&q=spring+is+a+new+service+instance+created#hl=en&sugexp=ciatsh&gs_nf=1&gs_mss=spring%20is%20a%20new%20bean%20instance%20created&tok=lnAt2g0iy8CWkY65Te75sg&pq=spring%20is%20a%20new%20bean%20instance%20created&cp=6&gs_id=1l&xhr=t&q=urlencode&pf=p&safe=off&sclient=psy-ab&oq=urls+en&gs_l=&pbx=1&bav=on.2,or.r_gc.r_pw.r_cp.r_qf.,cf.osb&fp=2176d1af1be1f17d&biw=1680&bih=965");
    }
}
