package org.apache.streams.instagram.provider;


import org.jinstagram.auth.model.Token;

/**
 * Extends JInstagram Token. Only difference is it overrides the equal method and determines equality based on the
 * token string.
 */
public class InstagramOauthToken extends Token {


    public InstagramOauthToken(String token) {
        this(token, null);
    }

    public InstagramOauthToken(String token, String secret) {
        super(token, secret);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof InstagramOauthToken)) {
            return false;
        }
        InstagramOauthToken that = (InstagramOauthToken) o;
        return this.getToken().equals(that.getToken());
    }
}
