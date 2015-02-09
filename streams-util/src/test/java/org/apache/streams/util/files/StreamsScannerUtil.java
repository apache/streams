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
