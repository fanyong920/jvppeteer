package com.ruiyun.jvppeteer.protocol.webAuthn;

/**
 * 验证信息
 */
public class Credentials {

    private String username;

    private String password;

    public Credentials() {
        super();
    }

    public Credentials(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
