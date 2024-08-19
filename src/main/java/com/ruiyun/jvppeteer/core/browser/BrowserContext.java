package com.ruiyun.jvppeteer.core.browser;

import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.options.BrowserLaunchArgumentOptions;
import com.ruiyun.jvppeteer.options.TargetType;
import com.ruiyun.jvppeteer.transport.Connection;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 浏览器上下文
 */
public class BrowserContext extends EventEmitter<BrowserContext.BrowserContextEvent> {
	/**
	 *  浏览器对应的websocket client包装类，用于发送和接受消息
	 */
	private Connection connection;
	/**
	 * 浏览器上下文对应的浏览器，一个上下文只有一个浏览器，但是一个浏览器可能有多个上下文
	 */
	private Browser browser;
	/**
	 *浏览器上下文id
	 */
	private String id;
	public BrowserContext() {
		super();
	}
	private static final Map<String,String> WEB_PERMISSION_TO_PROTOCOL_PERMISSION = new HashMap<>(32);
	static {
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("geolocation","geolocation");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("midi","midi");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("notifications","notifications");
//		webPermissionToProtocol.put("push","push");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("camera","videoCapture");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("microphone","audioCapture");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("background-sync","backgroundSync");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("ambient-light-sensor","sensors");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("accelerometer","sensors");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("gyroscope","sensors");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("magnetometer","sensors");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("accessibility-events","accessibilityEvents");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("clipboard-read","clipboardReadWrite");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("clipboard-write","clipboardReadWrite");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("clipboard-sanitized-write","clipboardSanitizedWrite");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("payment-handler","paymentHandler");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("persistent-storage","durableStorage");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("idle-detection","idleDetection");
		WEB_PERMISSION_TO_PROTOCOL_PERMISSION.put("midi-sysex","midiSysex");
	}
	public BrowserContext(Connection connection, Browser browser, String contextId) {
		super();
		this.connection = connection;
		this.browser = browser;
		this.id = contextId;
	}
	public List<Target> targets() {
		return this.browser.targets().stream().filter(target -> target.browserContext() == this).collect(Collectors.toList());
	}

	public List<Page> pages(){
		return this.targets().stream().filter(target -> TargetType.PAGE.equals(target.type()) || (TargetType.OTHER.equals(target.type()) && this.browser.getIsPageTargetCallback() != null ? this.browser.getIsPageTargetCallback().apply(target) : true)).map(Target::page).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public void overridePermissions(String origin, List<String> permissions) {
		permissions.replaceAll(item -> {
			String protocolPermission = WEB_PERMISSION_TO_PROTOCOL_PERMISSION.get(item);
			ValidateUtil.assertArg(protocolPermission != null,"Unknown permission: "+item);
			return  protocolPermission;
		});
		Map<String,Object> params = new HashMap<>();
		params.put("origin",origin);
		params.put("browserContextId",this.id);
		params.put("permissions",permissions);
		this.connection.send("Browser.grantPermissions", params);
	}

	public void clearPermissionOverrides() {
		Map<String,Object> params = new HashMap<>();
		params.put("browserContextId",this.id);
		this.connection.send("Browser.resetPermissions", params);
	}
	public Page newPage() {
		synchronized (this){
			return this.browser.createPageInContext(this.id);
		}
	}
	public void close() {
		ValidateUtil.assertArg(StringUtil.isNotEmpty(this.id), "Non-incognito profiles cannot be closed!");
		 this.browser.disposeContext(this.id);
	}

	public boolean closed(){
		return !this.browser.browserContexts().contains(this);
	}

	public Target waitForTarget(Predicate<Target> predicate, int timeout) {
		Observable<Target> targetCreateObservable = Helper.fromEmitterEvent(this, BrowserContextEvent.TargetCreated);
		Observable<Target> TargetChangeObservable = Helper.fromEmitterEvent(this, BrowserContextEvent.TargetChanged);
		@NonNull Observable<@NonNull Target> targetsObservable =  Observable.fromIterable(this.targets());
		return Observable.mergeArray(targetCreateObservable, TargetChangeObservable, targetsObservable).filter(predicate::test).timeout(timeout, TimeUnit.MILLISECONDS).blockingFirst();
	}


	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Browser browser() {
		return browser;
	}
	public String getId() {
		return this.id;
	}
	public enum BrowserContextEvent {
		TargetChanged("targetchanged"),
		TargetCreated("targetcreated"),
		TargetDestroyed("targetdestroyed");
		private String eventName;
		BrowserContextEvent(String eventName) {
			this.eventName = eventName;
		}
		public String getEventName() {
			return eventName;
		}
	}

}
