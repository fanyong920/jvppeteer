package com.ruiyun.jvppeteer.browser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.options.ChromeArgOptions;
import com.ruiyun.jvppeteer.options.DefaultViewport;
import com.ruiyun.jvppeteer.protocol.target.Target;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.ValidateUtil;

public class Browser {
	
	private Connection connection;
	
	private List<String> contextIds;
	
	private boolean ignoreHTTPSErrors;
	
	private DefaultViewport defaultViewport;
	
	private BrowserRunner runner;
	
	private Map<String,Target> targets;
	
	private BrowserContext defaultContext;
	
	private Map<String,BrowserContext> contexts;
	
	public Browser(Connection connection, List<String> contextIds, boolean ignoreHTTPSErrors,
			DefaultViewport defaultViewport, BrowserRunner runner) {
		super();
		this.connection = connection;
		this.contextIds = contextIds;
		this.ignoreHTTPSErrors = ignoreHTTPSErrors;
		this.defaultViewport = defaultViewport;
		this.runner = runner;
		
		defaultContext = new BrowserContext(connection,this,null);
		contexts = new HashMap<>();
		if(ValidateUtil.isNotEmpty(contextIds)){
			for (String contextId : contextIds) {
				contexts.putIfAbsent(contextId, new BrowserContext(this.connection,this,contextId));
			}
		}
		targets = new HashMap<>();
	}
	
	public List<Target> targets(){
		return targets.values().stream().collect(Collectors.toList());
	}

	public static Browser create(Connection connection,List<String> contextIds,boolean ignoreHTTPSErrors,DefaultViewport defaultViewport,BrowserRunner runner) throws JsonProcessingException{
		Browser browser = new Browser(connection,contextIds,ignoreHTTPSErrors,defaultViewport,runner);
		Map<String,Object> params = new HashMap<>();
		params.put("discover",true);
		connection.send("Target.setDiscoverTargets",params);
		return browser;
	}
	
	public Target waitForTarget(Predicate<Target> predicate,ChromeArgOptions options){
		Target existingTarget = find(targets(),predicate);
		if(null != existingTarget){
			return existingTarget;
		}
		return null;
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
}
