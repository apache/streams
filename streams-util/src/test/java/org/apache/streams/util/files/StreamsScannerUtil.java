package org.apache.streams.util.files;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Test Utility for acquiring a Scanner that won't choke on unicode or odd line-breaks.
 */
public class StreamsScannerUtil {

    protected static Pattern newLinePattern = Pattern.compile("(\\r\\n?|\\n)", Pattern.MULTILINE);

    public static Scanner getInstance(String resourcePath) {

        InputStream testFileStream = StreamsScannerUtil.class.getResourceAsStream(resourcePath);
        return new Scanner(testFileStream, "UTF-8").useDelimiter(newLinePattern);

    };
}
