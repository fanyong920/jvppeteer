package com.ruiyun.jvppeteer.transport;

public interface ConnectionTransport {


    void send(String message);

    void onMessage(String message);

    void setConnection(Connection connection);

    void close();
}
