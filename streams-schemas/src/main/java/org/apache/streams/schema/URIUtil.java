package org.apache.streams.schema;

import com.google.common.base.Optional;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by sblackmon on 5/1/16.
 */
public class URIUtil {

    public static URI removeFragment(URI id) {
        return URI.create(StringUtils.substringBefore(id.toString(), "#"));
    }

    public static URI removeFile(URI id) {
        return URI.create(StringUtils.substringBeforeLast(id.toString(), "/"));
    }

    public static Optional<URI> safeResolve(URI absolute, String relativePart) {
        if( !absolute.isAbsolute()) return Optional.absent();
        try {
            return Optional.of(absolute.resolve(relativePart));
        } catch( IllegalArgumentException e ) {
            return Optional.absent();
        }
    }

}
