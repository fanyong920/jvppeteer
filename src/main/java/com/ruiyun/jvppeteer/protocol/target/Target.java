package com.ruiyun.jvppeteer.protocol.target;

import com.ruiyun.jvppeteer.browser.Browser;
import com.ruiyun.jvppeteer.browser.BrowserContext;
import com.ruiyun.jvppeteer.options.DefaultViewport;
import com.ruiyun.jvppeteer.page.Page;
import com.ruiyun.jvppeteer.page.TaskQueue;
import com.ruiyun.jvppeteer.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

public class Target {

	private static final Logger LOGGER = LoggerFactory.getLogger(Target.class);

	private TargetInfo targetInfo;

	private BrowserContext browserContext;

	private boolean ignoreHTTPSErrors;

	private DefaultViewport defaultViewport;

	private TaskQueue<?> screenshotTaskQueue;

	private String targetId;

	private Callable<Page> pagePromise;

	private Callable<?> workerPromise;

	private Callable isClosedPromise;

	private boolean isInitialized;
	private Page page;

	public Target(TargetInfo targetInfo, BrowserContext browserContext, boolean ignoreHTTPSErrors, DefaultViewport defaultViewport, TaskQueue<?> screenshotTaskQueue) {
		this.targetInfo = targetInfo;
		this.browserContext = browserContext;
		this.targetId = targetInfo.getTargetId();
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
		this.defaultViewport = defaultViewport;
		this.screenshotTaskQueue = screenshotTaskQueue;
		this.pagePromise = null;
		this.workerPromise = null;


		isInitialized = !"page".equals(this.targetInfo.getType()) || StringUtil.isEmpty(this.targetInfo.getUrl());
		if(isInitialized){//初始化

		}
	}

	public String type(){
		return null;
		
		
	}

	public boolean initializedCallback(boolean success){
		if(!success){
			return false;
		}
		Target opener = this.opener();
		if(opener == null || opener.getPagePromise() == null || "page".equals(this.type())){
			return true;
		}
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		Future<Page> future = executorService.submit(opener.getPagePromise());
		executorService.shutdown();
		try {
			Page page = future.get();
			return true;
		} catch (InterruptedException e) {
			LOGGER.error("initializad target fail:",e);
		} catch (ExecutionException e) {
			LOGGER.error("initializad target fail:",e);
		}
		//TODO
		return true;

	}

	public Target opener(){
		String openerId = this.targetInfo.getOpenerId();
		if(StringUtil.isEmpty(openerId)){
			return null;
		}
		return this.browser().getTargets().get(openerId);
	}

	private Browser browser() {
		return this.browserContext.getBrowser();
	}

	public Callable<Page> getPagePromise() {
		return pagePromise;
	}

	public void setPagePromise(Callable<Page> pagePromise) {
		this.pagePromise = pagePromise;
	}

	public Callable<?> getWorkerPromise() {
		return workerPromise;
	}

	public void setWorkerPromise(Callable<?> workerPromise) {
		this.workerPromise = workerPromise;
	}
}
