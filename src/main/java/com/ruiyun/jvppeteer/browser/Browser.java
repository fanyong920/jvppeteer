package com.ruiyun.jvppeteer.browser;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.exception.LaunchException;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.page.Page;
import com.ruiyun.jvppeteer.protocol.page.TaskQueue;
import com.ruiyun.jvppeteer.protocol.target.Target;
import com.ruiyun.jvppeteer.protocol.target.TargetInfo;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * 浏览器实例
 */
public class Browser implements Constant {

	/**
	 * 浏览器对应的websocket client包装类，用于发送和接受消息
	 */
	private Connection connection;

	/**
	 * 当前浏览器的端口
	 */
	private int port ;

	/**
	 * 当前浏览器的上下文id集合
	 */
	private List<String> contextIds;

	/**
	 * 是否忽略https错误
	 */
	private boolean ignoreHTTPSErrors;

	/**
	 * 浏览器内的页面视图
	 */
	private Viewport viewport;

	/**
	 * 启动浏览器的操作者
	 */
	private BrowserRunner runner;

	/**
	 * 当前浏览器内的所有页面，也包括浏览器自己，{@link Page}和 {@link Browser} 都属于target
	 */
	private Map<String,Target> targets;

	/**
	 * 默认浏览器上下文
	 */
	private BrowserContext defaultContext;

	/**
	 * 浏览器上下文
	 */
	private Map<String,BrowserContext> contexts;

	private TaskQueue screenshotTaskQueue;

	/**
	 * 初始化浏览器实例时用到的downLatch
	 */
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

	/**
	 * 获取浏览器的所有target
	 * @return 所有target
	 */
	public List<Target> targets(){
		return new ArrayList<>(targets.values());
	}

	/**
	 * 创建一个浏览器
	 * @param connection 浏览器对应的websocket client包装类
	 * @param contextIds 上下文id集合
	 * @param ignoreHTTPSErrors 是否忽略https错误
	 * @param viewport 视图
	 * @param runner 浏览器启动类
	 * @param timeout 启动超时时间
	 * @return 浏览器
	 * @throws InterruptedException 等待消息完成过程中可能被断而发生的异常
	 * @throws ExecutionException 增加事件监听可能发生的异常
	 */
	public static Browser create(Connection connection,List<String> contextIds,boolean ignoreHTTPSErrors,Viewport viewport,BrowserRunner runner,int timeout) throws InterruptedException {
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

	/**
	 * 浏览器启动时要增加的事件监听
	 * @param browser 当前浏览器
	 * @throws ExecutionException 发布事件可能产生的异常
	 */
	private static void addBrowserListener(Browser browser) {
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
		browser.getConnection().emit(defaultBrowserListener.getMothod(),defaultBrowserListener);
	}

	/**
	 * 当前浏览器有target创建时会调用的方法
	 * @param targetInfo 创建的target具体信息
	 */
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
		this.getTargets().putIfAbsent(targetInfo.getTargetId(),target);
	}

	/**
	 * 浏览器启动时必须初始化一个target
	 * @param predicate target的断言
	 * @param options 浏览器启动参数
	 * @return target
	 */
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

	/**
	 * 在当前浏览器上新建一个页面
	 * @return 新建页面
	 */
	public Page newPage() {
		return this.defaultContext.newPage();
	}

	/**
	 * 在当前浏览器上下文新建一个页面
	 * @param contextId 上下文id 如果为空，则使用默认上下文
	 * @return 新建页面
	 */
	public Page createPageInContext(String contextId)  {
		Map<String,Object> params = new HashMap<>();
		params.put("url","about:blank");
		params.put("browserContextId",contextId);
		JsonNode recevie = this.connection.send("Target.createTarget", params,true);
		if(recevie != null){
			Target target = this.targets.get(recevie.get(RECV_MESSAGE_TARFETINFO_TARGETID_PROPERTY).asText());
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
