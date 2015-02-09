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

package org.apache.streams.s3;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class S3PersistWriterTest {
    private S3PersistWriter s3PersistWriter;

    @After
    public void tearDown() {
        s3PersistWriter = null;
    }

    @Test(expected=IllegalArgumentException.class)
    public void testBadS3Config() {
        s3PersistWriter = new S3PersistWriter(getBadConfig());

        s3PersistWriter.prepare(null);
    }

    @Test
    public void testGoodS3Config() {
        s3PersistWriter = new S3PersistWriter(getGoodConfig());

        s3PersistWriter.prepare(null);

        assertNotNull(s3PersistWriter.getAmazonS3Client());
    }

    @Test
    public void testCleanup() {
        s3PersistWriter = new S3PersistWriter(getGoodConfig());

        s3PersistWriter.prepare(null);

        s3PersistWriter.cleanUp();
    }

    private S3WriterConfiguration getBadConfig() {
        S3WriterConfiguration s3WriterConfiguration = new S3WriterConfiguration();

        s3WriterConfiguration.setWriterPath("bad_path");
        s3WriterConfiguration.setBucket("random_bucket");

        return s3WriterConfiguration;
    }

    private S3WriterConfiguration getGoodConfig() {
        S3WriterConfiguration s3WriterConfiguration = new S3WriterConfiguration();

        s3WriterConfiguration.setWriterPath("good_path/");
        s3WriterConfiguration.setBucket("random_bucket");
        s3WriterConfiguration.setKey("key");
        s3WriterConfiguration.setProtocol(S3Configuration.Protocol.HTTP);
        s3WriterConfiguration.setSecretKey("secret!");
        s3WriterConfiguration.setWriterFilePrefix("prefix");

        return s3WriterConfiguration;
    }
}