package org.apache.streams.test.component;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;

/**
 * FOR TESTING PURPOSES ONLY.
 *
 * The StreamProvider reads from a File or InputStream.  Each line of the file will be emitted as the document of a
 * streams datum.
 *
 */
public class FileReaderProvider implements StreamsProvider {

    private String fileName;
    private InputStream inStream;
    private Scanner scanner;
    private StreamsDatumConverter converter;

    public FileReaderProvider(String filePathInResources, StreamsDatumConverter converter) {
        this.fileName = filePathInResources;
        this.converter = converter;
    }

    @Override
    public void startStream() {

    }

    @Override
    public StreamsResultSet readCurrent() {
        return new ResultSet();
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.scanner = new Scanner(FileReaderProvider.class.getResourceAsStream(this.fileName));
    }

    @Override
    public void cleanUp() {
        this.scanner.close();
    }

    private class ResultSet extends StreamsResultSet {

        public ResultSet() {
            super(null);
        }


        @Override
        public Iterator<StreamsDatum> iterator() {
            return new FileProviderIterator();
        }

        private class FileProviderIterator implements Iterator<StreamsDatum> {



            @Override
            public boolean hasNext() {
                return scanner.hasNextLine();
            }

            @Override
            public StreamsDatum next() {
                return converter.convert(scanner.nextLine());
            }

            @Override
            public void remove() {

            }
        }
    }
}
