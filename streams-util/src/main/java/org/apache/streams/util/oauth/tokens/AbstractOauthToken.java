package org.apache.streams.util.oauth.tokens;

/**
 *
 */
public abstract class AbstractOauthToken {

    /**
     * Must create equals method for all OauthTokens.
     * @param o
     * @return true if equal, and false otherwise
     */
    protected abstract boolean internalEquals(Object o);

    @Override
    public boolean equals(Object o) {
        return this.internalEquals(o);
    }
}
