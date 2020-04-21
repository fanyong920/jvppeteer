package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.Constant;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;

import java.net.URI;
import java.util.Map;
import java.util.function.Consumer;

public abstract class ConnectionTransport extends WebSocketClient implements Constant {


	public ConnectionTransport(URI serverUri) {
		super(serverUri);
	}

	public ConnectionTransport(URI serverUri, Draft protocolDraft) {
		super(serverUri, protocolDraft);
	}

	public ConnectionTransport(URI serverUri, Map<String, String> httpHeaders) {
		super(serverUri, httpHeaders);
	}

	public ConnectionTransport(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
		super(serverUri, protocolDraft, httpHeaders);
	}

	public ConnectionTransport(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
		super(serverUri, protocolDraft, httpHeaders, connectTimeout);
	}

	public abstract void addMessageConsumer(Consumer<String> consumer);
}
