package org.apache.streams.util.oauth.tokens.tokenmanager.impl;

import org.apache.streams.util.oauth.tokens.AbstractOauthToken;
import org.apache.streams.util.oauth.tokens.tokenmanager.SimpleTokenManager;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Manages a pool of tokens the most basic possible way.  If all tokens are added to the manager before {@link BasicTokenManger#getNextAvailableToken() getNextAvailableToken}
 * is called tokens are issued in the order they were added to the manager, FIFO.  The BasicTokenManager acts as a circular queue
 * of tokens.  Once the manager issues all available tokens it will cycle back to the first token and start issuing tokens again.
 *
 * When adding tokens to the pool of available tokens, the manager will not add tokens that are already in the pool.
 *
 * The manager class is thread safe.
 */
public class BasicTokenManger<T> implements SimpleTokenManager<T>{

    private ArrayList<T> availableTokens;
    private int nextToken;

    public BasicTokenManger() {
        this(null);
    }

    public BasicTokenManger(Collection<T> tokens) {
        if(tokens != null) {
            this.availableTokens = new ArrayList<T>(tokens.size());
            this.addAllTokensToPool(tokens);
        } else {
            this.availableTokens = new ArrayList<T>();
        }
        this.nextToken = 0;
    }

    @Override
    public synchronized boolean addTokenToPool(T token) {
        if(token == null || this.availableTokens.contains(token))
            return false;
        else
            return this.availableTokens.add(token);
    }

    @Override
    public synchronized boolean addAllTokensToPool(Collection<T> tokens) {
        int startSize = this.availableTokens.size();
        for(T token : tokens) {
            this.addTokenToPool(token);
        }
        return startSize < this.availableTokens.size();
    }

    @Override
    public synchronized T getNextAvailableToken() {
        T token = null;
        if(this.availableTokens.size() == 0) {
            return token;
        } else {
            token = this.availableTokens.get(nextToken++);
            if(nextToken == this.availableTokens.size()) {
                nextToken = 0;
            }
            return token;
        }
    }

    @Override
    public synchronized int numAvailableTokens() {
        return this.availableTokens.size();
    }
}
