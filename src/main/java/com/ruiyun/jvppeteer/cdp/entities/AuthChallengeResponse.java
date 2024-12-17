package com.ruiyun.jvppeteer.cdp.entities;

public class AuthChallengeResponse {
    private String response = "Default";
    private String username = "";
    private String password = "";
    public AuthChallengeResponse()
    {

    }
    public AuthChallengeResponse(String response, String username, String password)
    {
        this.response = response;
        this.username = username;
        this.password = password;
    }
    public String getResponse() {
        return response;
    }
    public void setResponse(String response) {
        this.response = response;
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
