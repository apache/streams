package org.apache.streams.util.oauth.tokens.tokenmanager;

import org.apache.streams.util.oauth.tokens.AbstractOauthToken;

import java.util.Collection;

/**
 * Manges access to oauth tokens.  Allows a caller to add tokens to the token pool and receive an available token.
 */
public interface SimpleTokenManager<T> {


    /**
     * Adds a token to the available token pool.
     * @param token Token to be added
     * @return true, if token was successfully added to the pool and false otherwise.
     */
    public boolean addTokenToPool(T token);

    /**
     * Adds a {@link java.util.Collection} of tokens to the available token pool.
     * @param tokens Tokens to be added
     * @return true, if the token pool size increased after adding the tokens, and false otherwise.
     */
    public boolean addAllTokensToPool(Collection<T> tokens);

    /**
     * Get an available token. If no tokens are available it returns null.
     * @return next available token
     */
    public T getNextAvailableToken();

    /**
     * Get the number of available tokens
     * @return number of available tokens
     */
    public int numAvailableTokens();

}
