package org.apache.streams.data.moreover;

import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class MoreoverResultTest {


    @Test
    public void testMoreoverResultProcess() {
        MoreoverResult result = new MoreoverResult("", getResultString("/MoreoverXMLResponse.xml"), 0, 0);
        result.process();
        assertNotNull(result.getArticles());
        assertEquals(500, result.getArticles().size());

        MoreoverResult result2 = new MoreoverResult("", getResultString("/MoreoverXMLResponse2.xml"), 0, 0);
        result2.process();
        assertNotNull(result2.getArticles());
        assertEquals(1,result2.getArticles().size());
    }


    private String getResultString(String path) {
        StringBuilder sb = new StringBuilder();
        try(Scanner scanner = new Scanner(this.getClass().getResourceAsStream(path))) {
            while(scanner.hasNextLine()) {
                sb.append(scanner.nextLine());
            }
        }
        return sb.toString();
    }
}
