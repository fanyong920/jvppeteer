package com.ruiyun.jvppeteer.bidi.entities;

import com.ruiyun.jvppeteer.common.WebPermission;

public class PermissionOverride {
    private String origin;
    private WebPermission permission;

    public PermissionOverride(String origin, WebPermission permission) {
        this.origin = origin;
        this.permission = permission;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public WebPermission getPermission() {
        return permission;
    }

    public void setPermission(WebPermission permission) {
        this.permission = permission;
    }
}
