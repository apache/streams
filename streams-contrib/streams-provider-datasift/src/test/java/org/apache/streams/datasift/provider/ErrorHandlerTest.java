package org.apache.streams.datasift.provider;

import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 */
public class ErrorHandlerTest {

    @Test
    public void testErrorHandler() {
        DatasiftStreamProvider mockProvider = Mockito.mock(DatasiftStreamProvider.class);
        String streamHash = "fakeHash1";
        ErrorHandler handler = new ErrorHandler(mockProvider, streamHash);
        handler.exceptionCaught(new Exception("TEST EXCEPTION"));
        Mockito.verify(mockProvider).startStreamForHash(streamHash);
    }



}
