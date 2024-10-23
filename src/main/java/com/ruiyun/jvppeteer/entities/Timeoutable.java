package com.ruiyun.jvppeteer.entities;

import com.ruiyun.jvppeteer.common.Constant;

public class Timeoutable {

    /**
     * 最大导航时间是30000ms,0表示无限等待
     * <br/>
     * Maximum navigation time in milliseconds, pass 0 to disable timeout.
     * 默认是 30000
     */
    private int timeout = Constant.DEFAULT_TIMEOUT;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

}
