package org.apache.streams.s3;

import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class S3PersistReaderTest {

    @Test
    public void testUseTimestampAsPublishedEnabled() {
        S3ReaderConfiguration config = new S3ReaderConfiguration();
        config.setUseTimestampAsPublished(true);

        S3PersistReader s3PersistReader = new S3PersistReader(config);
        S3PersistReaderTask s3PersistReaderTask = new S3PersistReaderTask(s3PersistReader);

        DateTime publishedDate = s3PersistReaderTask.getPublishedDate(new String[]{"id", "2015-01-14T13:58:08.100-06:00", ""});
        DateTime actual = RFC3339Utils.parseToUTC(publishedDate.toString());
        DateTime expected = RFC3339Utils.parseToUTC("2015-01-14T13:58:08.100-06:00");

        assertEquals(expected, actual);
    }

    @Test
    public void testUseTimestampAsPublishedDisabled() {
        S3ReaderConfiguration config = new S3ReaderConfiguration();
        config.setUseTimestampAsPublished(false);

        S3PersistReader s3PersistReader = new S3PersistReader(config);
        S3PersistReaderTask s3PersistReaderTask = new S3PersistReaderTask(s3PersistReader);

        DateTime publishedDate = s3PersistReaderTask.getPublishedDate(new String[]{"id", "2015-01-14T13:58:08.100-06:00", ""});

        assertEquals(publishedDate, null);
    }

    @Test
    public void testUseTimestampAsPublishedNull() {
        S3ReaderConfiguration config = new S3ReaderConfiguration();

        S3PersistReader s3PersistReader = new S3PersistReader(config);
        S3PersistReaderTask s3PersistReaderTask = new S3PersistReaderTask(s3PersistReader);

        DateTime publishedDate = s3PersistReaderTask.getPublishedDate(new String[]{"id", "2015-01-14T13:58:08.100-06:00", ""});

        assertEquals(publishedDate, null);
    }

    @Test
    public void testUseTimestampAsPublishedInvalid() {
        S3ReaderConfiguration config = new S3ReaderConfiguration();

        S3PersistReader s3PersistReader = new S3PersistReader(config);
        S3PersistReaderTask s3PersistReaderTask = new S3PersistReaderTask(s3PersistReader);

        DateTime publishedDate = s3PersistReaderTask.getPublishedDate(new String[]{"id", "", ""});

        assertEquals(publishedDate, null);
    }
}
