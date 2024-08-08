package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.core.page.Page;

import java.util.List;

/**
 * ${@link Page#goTo}
 * 导航到页面的用的
 */
public class GoToOptions {

    /**
     * Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders().
     */
    private String referer;

    /**
     * 导航到一个页面的超时事件
     */
    private Integer timeout;
    /**
     * Referrer header value. If provided it will take preference over the referrer header value set by page.setExtraHTTPHeaders().
     */
    private String referrerPolicy;
    /**
     *  到哪个阶段才算导航完成，共有四个阶段
     * load -
     * domcontentloaded -
     * networkidle0 -
     * networkidle2 -
     */
    private List<PuppeteerLifeCycle> waitUntil;

    public GoToOptions() {
        super();
    }


    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public List<PuppeteerLifeCycle> getWaitUntil() {
        return waitUntil;
    }

    public void setWaitUntil(List<PuppeteerLifeCycle> waitUntil) {
        this.waitUntil = waitUntil;
    }
    public String getReferrerPolicy() {
        return referrerPolicy;
    }
    public void setReferrerPolicy(String referrerPolicy) {
        this.referrerPolicy = referrerPolicy;
    }
}
