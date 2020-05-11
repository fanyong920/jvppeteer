//package com.ruiyun.jvppeteer.transport.factory;
//
//import com.ruiyun.jvppeteer.Constant;
//import org.glassfish.tyrus.client.ClientManager;
//import org.glassfish.tyrus.container.grizzly.client.GrizzlyClientContainer;
//
//public class WebsocketContainerFactory implements Constant {
//
//	public static ClientManager create() {
//		ClientManager client = ClientManager.createClient(GrizzlyClientContainer.class.getCanonicalName());
//		client.getProperties().put(INCOMING_BUFFER_SIZE_PROPERTY, DEFAULT_PAYLOAD);
//		return client;
//	}
//}
