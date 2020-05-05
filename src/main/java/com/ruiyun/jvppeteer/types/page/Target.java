package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.definition.Events;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.transport.factory.SessionFactory;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.types.browser.Browser;
import com.ruiyun.jvppeteer.types.browser.BrowserContext;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Target {

	private  Boolean initializedPromise;

	private CountDownLatch initializedCountDown;

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
	private Worker workerPromise;

	@JsonIgnore
	private boolean isInitialized;

	@JsonIgnore
	private SessionFactory sessionFactory;

	private String sessionId;

	private CountDownLatch isClosedPromiseLatch;

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

		this.isInitialized = !"page".equals(this.targetInfo.getType()) || StringUtil.isEmpty(this.targetInfo.getUrl());
		if(isInitialized){//初始化
			this.initializedPromise = this.initializedCallback(true);
		}else{
			this.initializedPromise = true;
		}
	}
	public CDPSession createCDPSession() {
		return this.sessionFactory.create();
	}

	public Worker worker() {
		if ( !"service_worker".equals(this.targetInfo.getType()) && !"shared_worker".equals(this.targetInfo.getType()))
			return null;
		if (this.workerPromise == null) {
			// TODO(einbinder): Make workers send their console logs.this.workerPromise =
			synchronized (this){
				if (this.workerPromise == null) {
					CDPSession client = this.sessionFactory.create();
					this.workerPromise =  new Worker(client, this.targetInfo.getUrl(), (arg0,arg1,arg2) -> {} /* consoleAPICalled */, (arg) -> {} /* exceptionThrown */);
				}
			}

		}
		return this.workerPromise;
	}


	public void closedCallback(){
		if(pagePromise != null){
			this.pagePromise.emit(Events.PAGE_CLOSE.getName(),null);
			this.pagePromise.setClosed(true);
		}
		if(this.isClosedPromiseLatch != null){
			this.isClosedPromiseLatch.countDown();
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
		try {
			if(!success){
				this.initializedPromise = false;
				return false;
			}
			Target opener = this.opener();
			if(opener == null || opener.getPagePromise() == null || "page".equals(this.type())){
				this.initializedPromise = true;
				return true;
			}
			Page openerPage = opener.getPagePromise();
			if(openerPage.getListenerCount(Events.PAGE_POPUP.getName()) <= 0){
				this.initializedPromise = true;
				return true;
			}
			Page pupopPage = this.page();
			pupopPage.emit(Events.PAGE_POPUP.getName(),pupopPage);
			this.initializedPromise = true;
			return true;
		} finally {
			if(initializedCountDown != null && initializedCountDown.getCount() > 0){
				initializedCountDown.countDown();
				initializedCountDown = null;
			}
		}
	}

	public boolean waitInitializedPromise() {
		if(initializedPromise == null){
			this.initializedCountDown = new CountDownLatch(1);
			try {
				initializedCountDown.await(Constant.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				throw new RuntimeException("Wait for InitializedPromise fail:",e);
			}
		}
		return this.initializedPromise;
	}
	private Target opener(){
		String openerId = this.targetInfo.getOpenerId();
		if(StringUtil.isEmpty(openerId)){
			return null;
		}
		return this.browser().getTargets().get(openerId);
	}

	public String url() {
		return this.targetInfo.getUrl();
	}

	public Browser browser() {
		return this.browserContext.browser();
	}

	public Page getPagePromise() {
		return pagePromise;
	}

	public void setPagePromise(Page pagePromise) {
		this.pagePromise = pagePromise;
	}

	public Worker getWorkerPromise() {
		return workerPromise;
	}

	public void setWorkerPromise(Worker workerPromise) {
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

	public BrowserContext browserContext() {
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


	public boolean getIsInitialized() {
		return isInitialized;
	}

	public void setInitialized(boolean initialized) {
		isInitialized = initialized;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void targetInfoChanged(TargetInfo targetInfo) {
		this.targetInfo = targetInfo;
		if (!this.isInitialized && (!"page".equals(this.targetInfo.getType()) || !"".equals(this.targetInfo.getUrl()))) {
			this.isInitialized = true;
			this.initializedCallback(true);
			return;
		}
	}

	public boolean WaiforisClosedPromise(){
		this.isClosedPromiseLatch = new CountDownLatch(1);
		try {
			return this.isClosedPromiseLatch.await(Constant.DEFAULT_TIMEOUT,TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return  false;
	}

}
