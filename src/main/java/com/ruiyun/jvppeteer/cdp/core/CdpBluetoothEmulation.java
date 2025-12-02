package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.BluetoothEmulation;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.PreconnectedPeripheral;
import java.util.Map;

public class CdpBluetoothEmulation implements BluetoothEmulation {

    private final Connection connection;

    public CdpBluetoothEmulation(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void emulateAdapter(String state, boolean leSupported) {
        // Bluetooth spec requires overriding the existing adapter (step 6). From the CDP
        // perspective, it means disabling the emulation first.
        // https://webbluetoothcg.github.io/web-bluetooth/#bluetooth-simulateAdapter-command
        this.connection.send("BluetoothEmulation.disable");
        Map<String, Object> params = ParamsFactory.create();
        params.put("state", state);
        params.put("leSupported", leSupported);
        this.connection.send("BluetoothEmulation.enable", params);
    }

    @Override
    public void disableEmulation() {
        this.connection.send("BluetoothEmulation.disable");
    }

    @Override
    public void simulatePreconnectedPeripheral(PreconnectedPeripheral preconnectedPeripheral) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("preconnectedPeripheral", preconnectedPeripheral);
        this.connection.send("BluetoothEmulation.simulatePreconnectedPeripheral", params);
    }
}
