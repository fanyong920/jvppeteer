package com.ruiyun.jvppeteer.browser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserPublisher;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.protocol.page.TaskQueue;
import com.ruiyun.jvppeteer.protocol.page.frame.Target;
import com.ruiyun.jvppeteer.protocol.target.TargetInfo;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.Factory;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Browser implements Constant {
	
	private Connection connection;

	private String host = "localhost";

	private int port ;

	private String adderss;
	
	private List<String> contextIds;
	
	private boolean ignoreHTTPSErrors;
	
	private Viewport viewport;
	
	private BrowserRunner runner;
	
	private Map<String,Target> targets;
	
	private BrowserContext defaultContext;
	
	private Map<String,BrowserContext> contexts;

	private TaskQueue screenshotTaskQueue;

	private CountDownLatch downLatch = new CountDownLatch(2);
	
	public Browser(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors,
			Viewport defaultViewport, BrowserRunner runner) {
		super();
		this.connection = connection;
		this.contextIds = contextIds;
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
		this.viewport = defaultViewport;
		this.runner = runner;
		this.screenshotTaskQueue = new TaskQueue();
		defaultContext = new BrowserContext(connection,this,null);
		contexts = new HashMap<>();
		if(ValidateUtil.isNotEmpty(contextIds)){
			for (String contextId : contextIds) {
				contexts.putIfAbsent(contextId, new BrowserContext(this.connection,this,contextId));
			}
		}
		this.port = this.connection.getPort();

		targets = new HashMap<>();

	}
	
	public List<Target> targets(){
		return targets.values().stream().collect(Collectors.toList());
	}

	public static Browser create(Connection connection,List<String> contextIds,boolean ignoreHTTPSErrors,Viewport viewport,BrowserRunner runner,int timeout) throws InterruptedException, ExecutionException {
		Browser browser = new Browser(connection,contextIds,ignoreHTTPSErrors,viewport,runner);
		Map<String,Object> params = new HashMap<>();
		addBrowserListener(browser);
		//send
		params.put("discover",true);
		connection.send("Target.setDiscoverTargets",params,false);
		browser.getDownLatch().await(timeout, TimeUnit.MILLISECONDS);
		System.out.println("浏览器完成");
		return browser;
	}

	private static void addBrowserListener(Browser browser) throws ExecutionException {
		//先存发布者，再发送消息
		DefaultBrowserListener<Target> defaultBrowserListener = new DefaultBrowserListener<Target>() {
			@Override
			public void onBrowserEvent(Target event) {
				if("browser".equals(event.getTargetInfo().getType()) && event.getTargetInfo().getAttached()){
					Browser brow = (Browser)this.getTarget();
					brow.targetCreated(event.getTargetInfo());
					if(brow.getDownLatch() != null && brow.getDownLatch().getCount() > 0){
						brow.getDownLatch().countDown();
					}
				}
				if("page".equals(event.getTargetInfo().getType()) && !event.getTargetInfo().getAttached()  && Page.ABOUT_BLANK.equals(event.getTargetInfo().getUrl())){
					Browser brow = (Browser)this.getTarget();
					brow.targetCreated(event.getTargetInfo());
					if(brow.getDownLatch() != null && brow.getDownLatch().getCount() > 0){
						brow.getDownLatch().countDown();
					}
				}

			}
		};
		defaultBrowserListener.setTarget(browser);
		defaultBrowserListener.setMothod("Target.targetCreated");
		defaultBrowserListener.setResolveType(Target.class);
		Factory.get(DefaultBrowserPublisher.class.getSimpleName()+browser.getPort(),DefaultBrowserPublisher.class).addListener(defaultBrowserListener.getMothod(),defaultBrowserListener);
	}

	public void targetCreated(TargetInfo targetInfo){
		BrowserContext context = null;
		if(StringUtil.isNotEmpty( targetInfo.getBrowserContextId())){
			if( this.getContexts().containsKey(targetInfo.getBrowserContextId())){
				context = this.getContexts().get(targetInfo.getBrowserContextId());
			}else{
				context = this.getDefaultContext();
			}
		}
		Target target = new Target(targetInfo,context,() -> this.getConnection().createSession(targetInfo),this.getIgnoreHTTPSErrors(),this.getViewport(),this.getScreenshotTaskQueue());
		if(this.getTargets().get(targetInfo.getTargetId()) != null){
			throw new RuntimeException("Target should not exist before targetCreated");
		}
		System.out.println("put:"+targetInfo.getTargetId()+"target"+target);
		this.getTargets().putIfAbsent(targetInfo.getTargetId(),target);
	}
	
	public Target waitForTarget(Predicate<Target> predicate,ChromeArgOptions options){
		Target existingTarget = find(targets(),predicate);
		if(null != existingTarget){
			return existingTarget;
		}else{
			throw  new LaunchException("Waiting for target failed: timeout "+options.getTimeout()+"ms exceeded");
		}
	}

	public Target find(List<Target> targets,Predicate<Target> predicate){
		if(ValidateUtil.isNotEmpty(targets)){
			for (Target target : targets) {
				if(predicate.test(target)){
					return target;
				}
			}
		}
		return null;
	}

	public Page newPage() {
		return this.defaultContext.newPage();
	}

	public Page createPageInContext(String contextId)  {
		Map<String,Object> params = new HashMap<>();
		params.put("url","about:blank");
		params.put("browserContextId",contextId);
		Object recevie = this.connection.send("Target.createTarget", params,true);

//		//先存发布者，再发送消息
//		CountDownLatch downLatch = new CountDownLatch(1);
//		DefaultBrowserListener<Target> defaultBrowserListener = new DefaultBrowserListener<Target>() {
//			@Override
//			public void onBrowserEvent(Target event) {
//
//				if("page".equals(event.getTargetInfo().getType()) && !event.getTargetInfo().getAttached() && Page.ABOUT_BLANK.equals(event.getTargetInfo().getUrl())){
//					CountDownLatch downLatch = (CountDownLatch)this.getTarget();
//					downLatch.countDown();
//				}
//
//			}
//		};
//		defaultBrowserListener.setTarget(downLatch);
//		defaultBrowserListener.setMothod("Target.targetCreated");
//		defaultBrowserListener.setResolveType(Target.class);
//		Factory.get(DefaultBrowserPublisher.class.getSimpleName()+this.getPort(),DefaultBrowserPublisher.class).addListener(defaultBrowserListener.getMothod(),defaultBrowserListener);
//		downLatch.await(30000, TimeUnit.MILLISECONDS);
		if(recevie != null){
			JsonNode targetId = (JsonNode)recevie;
			Target target = this.targets.get(targetId.get(RECV_MESSAGE_TARFETINFO_TARGETID_PROPERTY).asText());
			try {
				System.out.println(OBJECTMAPPER.writeValueAsString(target));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return target.page();
		}
		throw new RuntimeException("can't create new page ,bacause recevie message is null");
	}

	public Map<String, Target> getTargets() {
		return targets;
	}

	public void setTargets(Map<String, Target> targets) {
		this.targets = targets;
	}

	public int getPort() {
		return this.port;
	}

	public CountDownLatch getDownLatch() {
		return downLatch;
	}

	public Map<String, BrowserContext> getContexts() {
		return contexts;
	}

	public void setContexts(Map<String, BrowserContext> contexts) {
		this.contexts = contexts;
	}

	public BrowserContext getDefaultContext() {
		return defaultContext;
	}

	public void setDefaultContext(BrowserContext defaultContext) {
		this.defaultContext = defaultContext;
	}

	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean getIgnoreHTTPSErrors() {
		return ignoreHTTPSErrors;
	}

	public void setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
	}

	public Viewport getViewport() {
		return viewport;
	}

	public void setViewport(Viewport viewport) {
		this.viewport = viewport;
	}

	public BrowserRunner getRunner() {
		return runner;
	}

	public void setRunner(BrowserRunner runner) {
		this.runner = runner;
	}

	public TaskQueue getScreenshotTaskQueue() {
		return screenshotTaskQueue;
	}

	public void setScreenshotTaskQueue(TaskQueue screenshotTaskQueue) {
		this.screenshotTaskQueue = screenshotTaskQueue;
	}
}
