package com.ruiyun.jvppeteer.api.core;

public abstract class TouchHandle {

    public abstract void move(double x,double y);

    public abstract void end();

    public void updateClient(CDPSession client) {

    }
}
