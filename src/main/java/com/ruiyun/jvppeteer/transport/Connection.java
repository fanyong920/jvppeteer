package com.ruiyun.jvppeteer.transport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEvent;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserEventPublisher;
import com.ruiyun.jvppeteer.events.browser.definition.BrowserListener;
import com.ruiyun.jvppeteer.events.browser.definition.Events;
import com.ruiyun.jvppeteer.events.browser.impl.DefaultBrowserListener;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.transport.message.SendMsg;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
/**
 * web socket client 浏览器级别的连接
 * @author fff
 *
 */
public class Connection implements Constant,Consumer<String>,BrowserEventPublisher{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	
	/**websoket url */
	private String url;
	
	private ConnectionTransport transport;
	/**
	 * The unit is millisecond
	 */
	private int delay;
	
	private static final AtomicLong adder = new AtomicLong(0);
	
	private  Map<Long,SendMsg> callbacks = new ConcurrentHashMap<>();//并发
	
	private Map<String,CDPSession> sessions = new HashMap<>();

	private Map<String, Set<DefaultBrowserListener>> listenerMap = new ConcurrentHashMap<>();

	public Connection( String url,ConnectionTransport transport, int delay) {
		super();
		this.url = url;
		this.transport = transport;
		this.delay = delay;
		this.transport.addMessageHandler(this);
	}
	
	public Object send(String method,Map<String, Object> params)  {
		SendMsg  message = new SendMsg();
		message.setMethod(method);
		message.setParams(params);
		try {
			long id = rawSend(message);
			if( id >= 0){
				callbacks.putIfAbsent(id, message);
				message.getSemaphore().tryAcquire(30,TimeUnit.SECONDS);
				return callbacks.remove(id).getResult();
			}
		} catch (InterruptedException e) {
			LOGGER.error("waiting message is interrupted,will not recevie any message about on this send ");
		}
		return null;
	}
	
	public long rawSend(SendMsg  message)  {
		long id = adder.incrementAndGet();
		message.setId(id);
		try {
			String sendMsg = OBJECTMAPPER.writeValueAsString(message);
			LOGGER.info("SEND ► "+sendMsg);
			transport.send(sendMsg);
			return id;
		}catch (JsonProcessingException e){
			LOGGER.error("parse message fail:",e);
		}
		return -1;
	}
	/**
	 * recevie message from browser by websocket
	 * @param message
	 */
	public void onMessage(String message) {
		if(delay >= 0) {
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				LOGGER.error("slowMo browser Fail:",e);
			}
		}
		
		LOGGER.info("◀ RECV "+message);
		System.out.println("◀ RECV "+message);
		try {
			if(StringUtil.isNotEmpty(message)) {
				JsonNode readTree = OBJECTMAPPER.readTree(message);
				JsonNode methodNode = readTree.get(RECV_MESSAGE_METHOD_PROPERTY);
				String method = methodNode.asText();
				if("Target.attachedToTarget".equals(method)) {//attached to target -> page attached to browser
					JsonNode paramsNode = readTree.get(RECV_MESSAGE_PARAMS_PROPERTY);
					JsonNode sessionId = paramsNode.get(RECV_MESSAGE_SESSION_ID_PROPERTY);
					JsonNode typeNode = paramsNode.get(RECV_MESSAGE_TARGETINFO_PROPERTY).get(RECV_MESSAGE_TYPE_PROPERTY);
					CDPSession cdpSession = new CDPSession(this,typeNode.asText(), sessionId.asText());
					sessions.put(sessionId.asText(), cdpSession);
				}else if("Target.detachedFromTarget".equals(method)) {//页面与浏览器脱离关系
					JsonNode paramsNode = readTree.get(RECV_MESSAGE_PARAMS_PROPERTY);
					JsonNode sessionId = paramsNode.get(RECV_MESSAGE_SESSION_ID_PROPERTY);
					String sessionIdString = sessionId.asText();
					CDPSession cdpSession = sessions.get(sessionIdString);
					if(cdpSession != null){
						cdpSession.onClosed();
						sessions.remove(sessionIdString);
					}
				}
				JsonNode objectSessionId = readTree.get(RECV_MESSAGE_SESSION_ID_PROPERTY);
				JsonNode objectId = readTree.get(RECV_MESSAGE_ID_PROPERTY);
				if(objectSessionId != null) {//cdpsession消息，当然cdpsession来处理
					String objectSessionIdString = objectSessionId.asText();
					CDPSession cdpSession = this.sessions.get(objectSessionIdString);
					if(cdpSession != null) {
						cdpSession.onMessage(readTree);
					}
				}else if(objectId != null) {//long类型的id,说明属于这次发送消息后接受的回应
					SendMsg sendMsg = this.callbacks.get(objectId.asLong());
					JsonNode error = readTree.get(RECV_MESSAGE_ERROR_PROPERTY);
					if(error != null){
						sendMsg.getSemaphore().release(1);
						throw new ProtocolException(Helper.createProtocolError(readTree));
					}else{
						JsonNode result = readTree.get(RECV_MESSAGE_RESULT_PROPERTY);
						sendMsg.setResult(result);
						sendMsg.getSemaphore().release(1);
					}
				}else{//是我们监听的事件，把它事件
					JsonNode paramsNode = readTree.get(RECV_MESSAGE_PARAMS_PROPERTY);
					publishEvent(method,paramsNode);
				}
			}
		} catch (Exception e) {
			LOGGER.error("parse recv message fail:",e);
		}
		
		
	}

	/**
	 * publish event
	 * @param method
	 * @param params
	 */

	protected void publishEvent(String method, JsonNode params){
		ValidateUtil.notNull(method, "method must not be null");
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);

		if(ValidateUtil.isNotEmpty(browserListeners)){
			try {
				Class<?> resolveType = browserListeners.stream().findFirst().get().getResolveType();
				BrowserEvent event = (BrowserEvent)readJsonObject(resolveType, params);
				publishEvent(method,event);
			}catch (IOException e){
				LOGGER.error("publish event error:",e);
			}
		}
	}

	private <T> T readJsonObject(Class<T> clazz, JsonNode jsonNode) throws IOException {
		if (jsonNode == null) {
			throw new IllegalArgumentException(
					"Failed converting null response to clazz " + clazz.getName());
		}
		return OBJECTMAPPER.readerFor(clazz).readValue(jsonNode);
	}

	public void invokeListener(BrowserListener listener,Object event){
			listener.onBrowserEvent((BrowserEvent) event);
	}

	public String url() {
		 return this.url;
	}
	
	public CDPSession session(String sessionId) {
	    return sessions.get(sessionId);
	 }

	@Override
	public void accept(String t) {
		onMessage(t);
	}


	@Override
	public void publishEvent(String method, Object event) {
		Set<DefaultBrowserListener> browserListeners = listenerMap.get(method);
		synchronized (transport){
			browserListeners = new LinkedHashSet(browserListeners);
		}
		for (DefaultBrowserListener listener : browserListeners) {
			executor.execute(() -> invokeListener(listener, event));
		}

	}
}
class CDPSession implements BrowserEventPublisher,Constant{

	private static final Logger LOGGER = LoggerFactory.getLogger(CDPSession.class);

	private  Map<Long,SendMsg> callbacks = new HashMap<Long, SendMsg>();
	
	private String targetType;
	
	private String sessionId;

	private Connection connection;
	
	public CDPSession(Connection connection,String targetType, String sessionId) {
		super();
		this.targetType = targetType;
		this.sessionId = sessionId;
		this.connection = connection;
	}

	public void onClosed() {
		for (SendMsg callback : callbacks.values()){
			LOGGER.error("Protocol error ("+callback.getMethod()+"): Target closed.");
		}
		connection = null;
		callbacks.clear();
		//TODO
		connection.publishEvent(Events.CDPSESSION_DISCONNECTED.getName(),null);
	}

	public Object send(String method,Map<String, Object> params)  {
		if(connection == null){
			throw new RuntimeException("Protocol error ("+method+"): Session closed. Most likely the"+this.targetType+"has been closed.");
		}
		SendMsg  message = new SendMsg();
		message.setMethod(method);
		message.setParams(params);
		long id = this.connection.rawSend(message);
		this.callbacks.putIfAbsent(id,message);
		try {
			message.getSemaphore().tryAcquire(30,TimeUnit.SECONDS);
			return callbacks.remove(id).getResult();
		} catch (InterruptedException e) {
			LOGGER.error("wait message result is interrupted:",e);
		}
		return null;
	}

	/**
	 * 页面分离浏览器
	 */
	public void detach(){
		if(connection == null){
			throw new RuntimeException("Session already detached. Most likely the"+this.targetType+"has been closed.");
		}
		Map<String,Object> params = new HashMap<>();
		params.put("sessionId",this.sessionId);
		this.connection.send("Target.detachFromTarget",params);
	}
	
	public void onMessage(JsonNode node) {
		JsonNode id = node.get(RECV_MESSAGE_ID_PROPERTY);
		if(id != null) {
			Long idLong = id.asLong();
			SendMsg callback = this.callbacks.get(idLong);
		    if (idLong != null && callback != null) {
		      JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
		      JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
		      if (errNode != null && errorMsg != null) {
				  callback.getSemaphore().release(1);
		    	  throw new ProtocolException(Helper.createProtocolError(node));
		      }else {
				  JsonNode result = node.get(RECV_MESSAGE_RESULT_PROPERTY);
				  callback.setResult(result);
				  callback.getSemaphore().release(1);
		      }
		    } else {
				JsonNode paramsNode = node.get(RECV_MESSAGE_PARAMS_PROPERTY);
				JsonNode method = node.get(RECV_MESSAGE_METHOD_PROPERTY);
				connection.publishEvent(method.asText(),paramsNode);
		    }
		}else {
			throw new RuntimeException("recv message id property is null");
		}
		
	  }

	@Override
	public void publishEvent(String method, Object event) {

	}
}
