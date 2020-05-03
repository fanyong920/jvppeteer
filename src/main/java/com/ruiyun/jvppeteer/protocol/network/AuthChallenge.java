package com.ruiyun.jvppeteer.protocol.network;

/**
 * Authorization challenge for HTTP status code 401 or 407.
 */
public class AuthChallenge {

    /**
     * Source of the authentication challenge.
     * "Server"|"Proxy"
     */
    private String source;
    /**
     * Origin of the challenger.
     */
    private String origin;
    /**
     * The authentication scheme used, such as basic or digest
     */
    private String scheme;
    /**
     * The realm of the challenge. May be empty.
     */
    private String realm;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }
}
