package org.apache.streams.test.component;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by rebanks on 2/27/14.
 */
public class ExpectedDatumsPersistWriter implements StreamsPersistWriter{

    private StreamsDatumConverter converter;
    private String fileName;
    private List<StreamsDatum> expectedDatums;
    private int counted = 0;
    private int expectedSize = 0;

    public ExpectedDatumsPersistWriter(StreamsDatumConverter converter, String filePathInResources) {
        this.converter = converter;
        this.fileName = filePathInResources;
    }



    @Override
    public void write(StreamsDatum entry) {
        int index = this.expectedDatums.indexOf(entry);
        assertNotEquals("Datum not expected. "+entry.toString(), -1, index);
        this.expectedDatums.remove(index);
        ++this.counted;
    }

    @Override
    public void prepare(Object configurationObject) {
        Scanner scanner = new Scanner(ExpectedDatumsPersistWriter.class.getResourceAsStream(this.fileName));
        this.expectedDatums = new LinkedList<StreamsDatum>();
        while(scanner.hasNextLine()) {
            this.expectedDatums.add(this.converter.convert(scanner.nextLine()));
        }
        this.expectedSize = this.expectedDatums.size();
    }

    @Override
    public void cleanUp() {
        assertEquals("Did not received the expected number of StreamsDatums", this.expectedSize, this.counted);
    }
}
