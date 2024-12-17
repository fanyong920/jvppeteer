package com.ruiyun.jvppeteer.transport;

import com.ruiyun.jvppeteer.api.core.Connection;

public interface ConnectionTransport {


    void send(String message);

    void onMessage(String message);

    void setConnection(Connection connection);

    void close();
}
