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
import com.ruiyun.jvppeteer.transport.message.SendMsg;
import com.ruiyun.jvppeteer.util.StringUtil;
/**
 * web socket client
 * @author fff
 *
 */
public class Connection implements Constant,Consumer<String> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(Connection.class);
	
	
	/**websoket url */
	private String url;
	
	private ConnectionTransport transport;
	/**
	 * The unit is millisecond
	 */
	private int delay;
	
	private static final AtomicLong adder = new AtomicLong(0);
	
	private static final Map<Long,Object> _CALLBACKS = new HashMap<Long, Object>();
	
	private Map<String,CDPSession> _sessions = new HashMap<>();
	
	public Connection( String url,ConnectionTransport transport, int delay) {
		super();
		this.url = url;
		this.transport = transport;
		this.delay = delay;
		this.transport.addMessageHandler(this);
	}
	
	public void send(String method,Map<String, Object> params) throws JsonProcessingException {
		SendMsg  message = new SendMsg();
		message.setMethod(method);
		message.setParams(params);
		long id = _rawSend(message);
		_CALLBACKS.putIfAbsent(id, method);
	}
	
	public long _rawSend(SendMsg  message) throws JsonProcessingException {
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
	public void _onMessage(String message) {
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
					JsonNode paramsNode = methodNode.get(RECV_MESSAGE_PARAMS_PROPERTY);
					JsonNode sessionId = paramsNode.get(RECV_MESSAGE_ID_PROPERTY);
					JsonNode typeNode = paramsNode.get(RECV_MESSAGE_TARGETINFO_PROPERTY).get(RECV_MESSAGE_TYPE_PROPERTY);
					CDPSession cdpSession = new CDPSession(this,typeNode.asText(), sessionId.asText());
					_sessions.put(sessionId.asText(), cdpSession);
				}else if("Target.detachedFromTarget".equals(method)) {
					JsonNode paramsNode = methodNode.get(RECV_MESSAGE_PARAMS_PROPERTY);
					JsonNode sessionId = paramsNode.get(RECV_MESSAGE_ID_PROPERTY);
					CDPSession cdpSession = _sessions.get(sessionId);
					if(cdpSession != null){
						cdpSession._onClosed();
						_sessions.remove(sessionId);
					}
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
	    return _sessions.get(sessionId);
	 }

	@Override
	public void accept(String t) {
		_onMessage(t);
	}
	
}
class CDPSession{
	
	private String targetType;
	
	private String sessionId;

	private Connection connection;
	
	public CDPSession(Connection connection,String targetType, String sessionId) {
		super();
		this.targetType = targetType;
		this.sessionId = sessionId;
		this.connection = connection;
	}

	public void _onClosed() {
		// TODO Auto-generated method stub
		connection = null;
	}
	
	
}
