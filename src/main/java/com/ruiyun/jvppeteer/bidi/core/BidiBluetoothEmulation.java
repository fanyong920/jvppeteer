package com.ruiyun.jvppeteer.bidi.core;

import com.ruiyun.jvppeteer.api.core.BluetoothEmulation;
import com.ruiyun.jvppeteer.common.AdapterState;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.PreconnectedPeripheral;
import java.util.Map;

public class BidiBluetoothEmulation implements BluetoothEmulation {

    private final Session session;
    public String contextId;

    public BidiBluetoothEmulation(String contextId, Session session) {
        this.session = session;
        this.contextId = contextId;
    }

    @Override
    public void emulateAdapter(AdapterState state, boolean leSupported) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.contextId);
        params.put("state", state.getState());
        params.put("leSupported", leSupported);
        this.session.send("bluetooth.simulateAdapter", params);
    }

    @Override
    public void disableEmulation() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.contextId);
        this.session.send("bluetooth.disableSimulation", params);
    }

    @Override
    public void simulatePreconnectedPeripheral(PreconnectedPeripheral preconnectedPeripheral) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("context", this.contextId);
        params.put("address", preconnectedPeripheral.getAddress());
        params.put("name", preconnectedPeripheral.getName());
        params.put("manufacturerData", preconnectedPeripheral.getManufacturerData());
        params.put("knownServiceUuids", preconnectedPeripheral.getKnownServiceUuids());
        this.session.send("bluetooth.simulatePreconnectedPeripheral", params);
    }
}

