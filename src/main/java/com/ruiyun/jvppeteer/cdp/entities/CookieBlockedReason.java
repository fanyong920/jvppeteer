package com.ruiyun.jvppeteer.cdp.entities;
/**
 * Types of reasons why a cookie may not be sent with a request.
 * @experimental
 */
public enum CookieBlockedReason {
    SecureOnly,
    NotOnPath,
    DomainMismatch,
    SameSiteStrict,
    SameSiteLax,
    SameSiteUnspecifiedTreatedAsLax,
    SameSiteNoneInsecure,
    UserPreferences,
    ThirdPartyPhaseout,
    ThirdPartyBlockedInFirstPartySet,
    UnknownError,
    SchemefulSameSiteStrict,
    SchemefulSameSiteLax,
    SchemefulSameSiteUnspecifiedTreatedAsLax,
    SamePartyFromCrossPartyContext,
    NameValuePairExceedsMaxSize,
    PortMismatch,
    SchemeMismatch,
    AnonymousContext;
}
