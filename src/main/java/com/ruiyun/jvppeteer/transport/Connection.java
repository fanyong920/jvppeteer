package com.ruiyun.jvppeteer.transport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.events.definition.AbstractBrowserListener;
import com.ruiyun.jvppeteer.events.definition.BrowserEventPublisher;
import com.ruiyun.jvppeteer.exception.ProtocolException;
import com.ruiyun.jvppeteer.transport.message.SendMsg;
import com.ruiyun.jvppeteer.util.StringUtil;
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
	
	private  Map<Long,Long> callbacks = new HashMap<Long, Long>();
	
	private Map<String,CDPSession> sessions = new HashMap<>();
	
	public Connection( String url,ConnectionTransport transport, int delay) {
		super();
		this.url = url;
		this.transport = transport;
		this.delay = delay;
		this.transport.addMessageHandler(this);
	}
	
	public String send(String method,Map<String, Object> params) throws JsonProcessingException {
		SendMsg  message = new SendMsg();
		message.setMethod(method);
		message.setParams(params);
		long id = rawSend(message);
		callbacks.putIfAbsent(id, id);
		return null;
	}
	
	public long rawSend(SendMsg  message) throws JsonProcessingException {
		long id = adder.incrementAndGet();
		message.setId(id);
		String sendMsg = OBJECTMAPPER.writeValueAsString(message);
		LOGGER.info("SEND ► "+sendMsg);
		transport.send(sendMsg);
		return id;
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
				}else if("Target.detachedFromTarget".equals(method)) {
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
				if(objectSessionId != null) {
					String objectSessionIdString = objectSessionId.asText();
					CDPSession cdpSession = this.sessions.get(objectSessionIdString);
					if(cdpSession != null) {
//						cdpSession.o
						cdpSession.onMessage(readTree);
					}
				}else if(false) {
					
				}
			}
			
		} catch (Exception e) {
			LOGGER.error("parse recv message fail:",e);
		}
		
		
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
	
	/**
	 * publish event
	 */
	@Override
	public void publishEvent(Object event) {
		
		
	}
	
}
class CDPSession implements BrowserEventPublisher,Constant{
	 
	private  Map<Long,Object> callbacks = new HashMap<Long, Object>();
	
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
		// TODO Auto-generated method stub
		connection = null;
		callbacks.clear();
		this.publishEvent("");
	}

	@Override
	public void publishEvent(Object event) {
		
		
	}
	
	public void onMessage(JsonNode node) {
		JsonNode id = node.get(RECV_MESSAGE_ID_PROPERTY);
		if(id != null) {
			String idText = id.asText();
			Object callback = this.callbacks.get(idText);
		    if (StringUtil.isNotEmpty(idText) && callback != null) {
		    	this.callbacks.remove(callback);
		      JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
		      JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
		      if (errNode != null && errorMsg != null) {
		    	  throw new ProtocolException(createProtocolError(node));
		      }else {
		    	  //TODO
//		    	  callback.
		      }
		    } else {
		    	throw new RuntimeException("recv message id property is null");
		    }
		}else {
			throw new RuntimeException("recv message id property is null");
		}
		
	  }
	public String createProtocolError(JsonNode node) {
		JsonNode methodNode = node.get(RECV_MESSAGE_METHOD_PROPERTY);
		JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
	    JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
		String message = "Protocol error "+methodNode.asText()+": "+errorMsg;
		JsonNode dataNode = errNode.get(RECV_MESSAGE_ERROR_DATA_PROPERTY);
		 if(dataNode != null) {
			 message += " "+dataNode.asText();
		 }
		 return message;
	}
	
}
