package com.ruiyun.jvppeteer.protocol.page.frame;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.browser.BrowserContext;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.protocol.page.TaskQueue;
import com.ruiyun.jvppeteer.protocol.promise.Promise;
import com.ruiyun.jvppeteer.protocol.target.TargetInfo;
import com.ruiyun.jvppeteer.transport.SessionFactory;
import com.ruiyun.jvppeteer.util.StringUtil;

public class Target {

	private TargetInfo targetInfo;

	@JsonIgnore
	private BrowserContext browserContext;

	@JsonIgnore
	private boolean ignoreHTTPSErrors;

	@JsonIgnore
	private Viewport viewport;

	@JsonIgnore
	private TaskQueue<?> screenshotTaskQueue;

	private String targetId;

	@JsonIgnore
	private Page pagePromise;

	@JsonIgnore
	private Promise<?> workerPromise;

	@JsonIgnore
	private Promise<Object> isClosedPromise;

	@JsonIgnore
	private boolean isInitialized;

	@JsonIgnore
	private Page page;

	@JsonIgnore
	private SessionFactory sessionFactory;


	public Target() {

	}

	public Target(TargetInfo targetInfo, BrowserContext browserContext, SessionFactory sessionFactory, boolean ignoreHTTPSErrors, Viewport defaultViewport, TaskQueue<?> screenshotTaskQueue) {
		this.targetInfo = targetInfo;
		this.browserContext = browserContext;
		this.targetId = targetInfo.getTargetId();
		this.sessionFactory = sessionFactory;
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
		this.viewport = defaultViewport;
		this.screenshotTaskQueue = screenshotTaskQueue;
		this.pagePromise = null;
		this.workerPromise = null;
        //TODO isClosedPromise

		isInitialized = !"page".equals(this.targetInfo.getType()) || StringUtil.isEmpty(this.targetInfo.getUrl());
		if(isInitialized){//初始化
            this.initializedCallback(true);
		}
	}

	public Page page(){
		String type ;
		if (("page".equals(type = this.targetInfo.getType()) || "background_page".equals(type)) && this.pagePromise == null) {
             this.pagePromise = Page.create(this.sessionFactory.create(), this, this.ignoreHTTPSErrors, this.viewport, this.screenshotTaskQueue);
        }
		return this.pagePromise;
	}

	public String type(){
        String type = this.targetInfo.getType();
        if("page".equals(type) || "background_page".equals(type) || "service_worker".equals(type) || "shared_worker".equals(type) ||"browser".equals(type)){
            return type;
        }
        return "other";
		
		
	}

	public boolean initializedCallback(boolean success){
		if(!success){
			return false;
		}
		Target opener = this.opener();
		if(opener == null || opener.getPagePromise() == null || "page".equals(this.type())){
			return true;
		}
        Page openerPage = opener.getPagePromise();
        if(openerPage.getListenerCount(Events.PAGE_POPUP.getName()) <= 0){
            return true;
        }
        Page pupopPage = this.page();
        pupopPage.emit(Events.PAGE_POPUP.getName(),pupopPage);
		return true;
	}

	private Target opener(){
		String openerId = this.targetInfo.getOpenerId();
		if(StringUtil.isEmpty(openerId)){
			return null;
		}
		return this.browser().getTargets().get(openerId);
	}

	private Browser browser() {
		return this.browserContext.getBrowser();
	}

	public Page getPagePromise() {
		return pagePromise;
	}

	public void setPagePromise(Page pagePromise) {
		this.pagePromise = pagePromise;
	}

	public Promise<?> getWorkerPromise() {
		return workerPromise;
	}

	public void setWorkerPromise(Promise<?> workerPromise) {
		this.workerPromise = workerPromise;
	}

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public TargetInfo getTargetInfo() {
		return targetInfo;
	}

	public void setTargetInfo(TargetInfo targetInfo) {
		this.targetInfo = targetInfo;
	}

	public BrowserContext getBrowserContext() {
		return browserContext;
	}

	public void setBrowserContext(BrowserContext browserContext) {
		this.browserContext = browserContext;
	}

	public boolean isIgnoreHTTPSErrors() {
		return ignoreHTTPSErrors;
	}

	public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
	}

	public Viewport getDefaultViewport() {
		return viewport;
	}

	public void setDefaultViewport(Viewport viewport) {
		this.viewport = viewport;
	}

	public TaskQueue<?> getScreenshotTaskQueue() {
		return screenshotTaskQueue;
	}

	public void setScreenshotTaskQueue(TaskQueue<?> screenshotTaskQueue) {
		this.screenshotTaskQueue = screenshotTaskQueue;
	}

	public Promise<Object> getIsClosedPromise() {
		return isClosedPromise;
	}

	public void setIsClosedPromise(Promise<Object> isClosedPromise) {
		this.isClosedPromise = isClosedPromise;
	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean initialized) {
		isInitialized = initialized;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}
}
