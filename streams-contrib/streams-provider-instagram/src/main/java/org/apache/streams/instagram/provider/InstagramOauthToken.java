package org.apache.streams.instagram.provider;

import org.apache.streams.util.oauth.tokens.OauthToken;

/**
 *
 */
public class InstagramOauthToken extends OauthToken{

    private String clientId;

    public InstagramOauthToken(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    protected boolean internalEquals(Object o) {
        if(!(o instanceof InstagramOauthToken)) {
            return false;
        }
        InstagramOauthToken that = (InstagramOauthToken) o;
        return this.clientId.equals(that.clientId);
    }
}
