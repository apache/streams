package org.apache.streams.core.builders;

/**
 * Exception that indicates a malformed data stream in some way.
 */
public class InvalidStreamException extends RuntimeException {

    public InvalidStreamException() {
        super();
    }

    public InvalidStreamException(String s) {
        super(s);
    }

    public InvalidStreamException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidStreamException(Throwable throwable) {
        super(throwable);
    }
}
