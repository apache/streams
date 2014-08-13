/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */
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
