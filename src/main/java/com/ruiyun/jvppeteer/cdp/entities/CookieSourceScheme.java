package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum CookieSourceScheme {
    @JsonProperty("Unset")
    Unset,
    @JsonProperty("NonSecure")
    NonSecure,
    @JsonProperty("Secure")
    Secure,
}
