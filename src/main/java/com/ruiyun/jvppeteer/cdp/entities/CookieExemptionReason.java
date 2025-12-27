package com.ruiyun.jvppeteer.cdp.entities;
/**
 * Types of reasons why a cookie should have been blocked by 3PCD but is exempted for the request.
 */
public enum CookieExemptionReason {
    None,
    UserSetting,
    TPCDMetadata,
    TPCDDeprecationTrial,
    TopLevelTPCDDeprecationTrial,
    TPCDHeuristics,
    EnterprisePolicy,
    StorageAccess,
    TopLevelStorageAccess,
    Scheme,
    SameSiteNoneCookiesInSandbox;
}
