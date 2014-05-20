package org.apache.streams.datasift.provider;

import com.datasift.client.stream.ErrorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens for exceptions from the datasift streams and resets connections on errors.
 */
public class ErrorHandler extends ErrorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorHandler.class);

    private String streamHash;
    private DatasiftStreamProvider provider;

    public ErrorHandler(DatasiftStreamProvider provider, String streamHash) {
        this.provider = provider;
        this.streamHash = streamHash;
    }

    @Override
    public void exceptionCaught(Throwable throwable) {
        LOGGER.error("DatasiftClient received Exception : {}", throwable);
        LOGGER.info("Attempting to restart client for stream hash : {}", this.streamHash);
        this.provider.startStreamForHash(this.streamHash);
    }
}
