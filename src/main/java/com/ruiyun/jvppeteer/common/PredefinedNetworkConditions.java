package com.ruiyun.jvppeteer.common;

import com.ruiyun.jvppeteer.cdp.entities.NetworkConditions;

public enum PredefinedNetworkConditions {
    // ~500Kbps down ~500Kbps up 400ms RTT
    Slow_3G(new NetworkConditions(((500 * 1000) / 8.0) * 0.8, ((500 * 1000) / 8.0) * 0.8, 400 * 0.5)),
    // ~1.6 Mbps down ~0.75 Mbps up  150ms RTT
    Fast_3G(new NetworkConditions(((1.6 * 1000 * 1000) / 8) * 0.9, ((750 * 1000) / 8.0) * 0.9, 150 * 3.75)),
    //~1.6 Mbps down ~0.75 Mbps up 150ms RTT
    Slow_4G(new NetworkConditions(((1.6 * 1000 * 1000) / 8) * 0.9, ((750 * 1000) / 8.0) * 0.9, 150 * 3.75)),
    // 9 Mbps down  1.5 Mbps up 60ms RTT
    Fast_4G(new NetworkConditions(((9 * 1000 * 1000) / 8.0) * 0.9, ((1.5 * 1000 * 1000) / 8) * 0.9, 60 * 2.75));
    private final NetworkConditions networkConditions;

    PredefinedNetworkConditions(NetworkConditions networkConditions) {
        this.networkConditions = networkConditions;
    }

    public NetworkConditions getNetworkConditions() {
        return networkConditions;
    }

}
