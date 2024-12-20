package com.ruiyun.jvppeteer.cdp.entities;

import java.util.Map;

/**
 * HTTP response data.
 */
public class ResponsePayload {

    /**
     * Response URL. This URL can be different from CachedResource.url in case of redirect.
     */
    private String url;
    /**
     * HTTP response status code.
     */
    private int status;
    /**
     * HTTP response status text.
     */
    private String statusText;
    /**
     * HTTP response headers.
     */
    private Map<String, String> headers;
    /**
     * HTTP response headers text.
     */
    private String headersText;
    /**
     * Resource mimeType as determined by the browser.
     */
    private String mimeType;
    /**
     * Resource charset as determined by the browser (if applicable).
     */
    private String charset;
    /**
     * Refined HTTP request headers that were actually transmitted over the network.
     */
    private Map<String, Object> requestHeaders;
    /**
     * HTTP request headers text.
     */
    private String requestHeadersText;
    /**
     * Specifies whether physical connection was actually reused for this request.
     */
    private boolean connectionReused;
    /**
     * Physical connection id that was actually used for this request.
     */
    private int connectionId;
    /**
     * Remote IP address.
     */
    private String remoteIPAddress;
    /**
     * Remote port.
     */
    private int remotePort;
    /**
     * Specifies that the request was served from the disk cache.
     */
    private boolean fromDiskCache;
    /**
     * Specifies that the request was served from the ServiceWorker.
     */
    private boolean fromServiceWorker;
    /**
     * Specifies that the request was served from the prefetch cache.
     */
    private boolean fromPrefetchCache;
    /**
     * Specifies that the request was served from the prefetch cache.
     */
    private boolean fromEarlyHints;
    private ServiceWorkerRouterInfo serviceWorkerRouterInfo;
    private int encodedDataLength;
    private ResourceTiming timing;
    private String serviceWorkerResponseSource;
    private long responseTime;
    private String cacheStorageCacheName;
    private String protocol;
    private String alternateProtocolUsage;
    private String securityState;
    private SecurityDetails securityDetails;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeadersText() {
        return headersText;
    }

    public void setHeadersText(String headersText) {
        this.headersText = headersText;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Map<String, Object> getRequestHeaders() {
        return requestHeaders;
    }

    public void setRequestHeaders(Map<String, Object> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    public String getRequestHeadersText() {
        return requestHeadersText;
    }

    public void setRequestHeadersText(String requestHeadersText) {
        this.requestHeadersText = requestHeadersText;
    }

    public boolean getConnectionReused() {
        return connectionReused;
    }

    public void setConnectionReused(boolean connectionReused) {
        this.connectionReused = connectionReused;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    public String getRemoteIPAddress() {
        return remoteIPAddress;
    }

    public void setRemoteIPAddress(String remoteIPAddress) {
        this.remoteIPAddress = remoteIPAddress;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public boolean getFromDiskCache() {
        return fromDiskCache;
    }

    public void setFromDiskCache(boolean fromDiskCache) {
        this.fromDiskCache = fromDiskCache;
    }

    public boolean getFromServiceWorker() {
        return fromServiceWorker;
    }

    public void setFromServiceWorker(boolean fromServiceWorker) {
        this.fromServiceWorker = fromServiceWorker;
    }

    public boolean getFromPrefetchCache() {
        return fromPrefetchCache;
    }

    public void setFromPrefetchCache(boolean fromPrefetchCache) {
        this.fromPrefetchCache = fromPrefetchCache;
    }

    public int getEncodedDataLength() {
        return encodedDataLength;
    }

    public void setEncodedDataLength(int encodedDataLength) {
        this.encodedDataLength = encodedDataLength;
    }

    public ResourceTiming getTiming() {
        return timing;
    }

    public void setTiming(ResourceTiming timing) {
        this.timing = timing;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSecurityState() {
        return securityState;
    }

    public void setSecurityState(String securityState) {
        this.securityState = securityState;
    }

    public SecurityDetails getSecurityDetails() {
        return securityDetails;
    }

    public void setSecurityDetails(SecurityDetails securityDetails) {
        this.securityDetails = securityDetails;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public String getCacheStorageCacheName() {
        return cacheStorageCacheName;
    }

    public void setCacheStorageCacheName(String cacheStorageCacheName) {
        this.cacheStorageCacheName = cacheStorageCacheName;
    }

    public String getAlternateProtocolUsage() {
        return alternateProtocolUsage;
    }

    public void setAlternateProtocolUsage(String alternateProtocolUsage) {
        this.alternateProtocolUsage = alternateProtocolUsage;
    }

    public String getServiceWorkerResponseSource() {
        return serviceWorkerResponseSource;
    }

    public void setServiceWorkerResponseSource(String serviceWorkerResponseSource) {
        this.serviceWorkerResponseSource = serviceWorkerResponseSource;
    }

    public ServiceWorkerRouterInfo getServiceWorkerRouterInfo() {
        return serviceWorkerRouterInfo;
    }

    public void setServiceWorkerRouterInfo(ServiceWorkerRouterInfo serviceWorkerRouterInfo) {
        this.serviceWorkerRouterInfo = serviceWorkerRouterInfo;
    }

    public boolean getFromEarlyHints() {
        return fromEarlyHints;
    }

    public void setFromEarlyHints(boolean fromEarlyHints) {
        this.fromEarlyHints = fromEarlyHints;
    }


    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
}
