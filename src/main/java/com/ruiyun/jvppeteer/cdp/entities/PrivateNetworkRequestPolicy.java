package com.ruiyun.jvppeteer.cdp.entities;

public enum PrivateNetworkRequestPolicy {
    Allow,
    BlockFromInsecureToMorePrivate,
    WarnFromInsecureToMorePrivate,
    PreflightBlock,
    PreflightWarn,
    PermissionBlock,
    PermissionWarn;
}
