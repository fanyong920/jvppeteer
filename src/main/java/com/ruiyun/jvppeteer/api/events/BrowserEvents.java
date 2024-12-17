package com.ruiyun.jvppeteer.api.events;

import com.ruiyun.jvppeteer.cdp.core.CdpTarget;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;

public enum BrowserEvents {
    /**
     * 创建target
     * {@link CdpTarget}
     */
    TargetCreated,
    /**
     * 销毁target
     * {@link CdpTarget}
     */
    TargetDestroyed,
    /**
     * target变化
     * {@link CdpTarget}
     */
    TargetChanged,
    /**
     * 发现target
     * {@link TargetInfo}
     */
    TargetDiscovered,
    /**
     * 断开连接
     * Object
     */
    Disconnected,
    /**
     * 下载进度时触发
     */
    DownloadProgress,

    /**
     * 当页面准备开始下载时促发
     */
    DownloadWillBegin
}
