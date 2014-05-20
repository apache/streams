package org.apache.streams.datasift.provider;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Requires Java version 1.7!
 */
public class ErrorHandlerTest {

    @Test @Ignore
    public void testErrorHandler() {
        DatasiftStreamProvider mockProvider = Mockito.mock(DatasiftStreamProvider.class);
        String streamHash = "fakeHash1";
        ErrorHandler handler = new ErrorHandler(mockProvider, streamHash);
        handler.exceptionCaught(new Exception("TEST EXCEPTION"));
        Mockito.verify(mockProvider).startStreamForHash(streamHash);
    }



}
