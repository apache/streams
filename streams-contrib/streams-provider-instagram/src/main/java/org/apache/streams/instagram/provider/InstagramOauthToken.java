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
