package com.ruiyun.jvppeteer.common;

public class Permission {
    private PermissionDescriptor permission;
    private PermissionState state;

    public PermissionDescriptor getPermission() {
        return permission;
    }

    public void setPermission(PermissionDescriptor permission) {
        this.permission = permission;
    }

    public PermissionState getState() {
        return state;
    }

    public void setState(PermissionState state) {
        this.state = state;
    }
}
