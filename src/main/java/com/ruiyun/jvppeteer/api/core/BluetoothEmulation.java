package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.common.PreconnectedPeripheral;

public  interface BluetoothEmulation {
    /**
     * Emulate Bluetooth adapter. Required for bluetooth simulations
     * See {@link <a href="https://webbluetoothcg.github.io/web-bluetooth/#bluetooth-simulateAdapter-command|bluetooth.simulateAdapter">Here</a>}.
     *
     * @param state - The desired bluetooth adapter state.
     * @param leSupported - Mark if the adapter supports low-energy bluetooth.
     *
     */
    public void emulateAdapter(String  state,boolean leSupported);
    /**
     * Disable emulated bluetooth adapter.
     * See {@link <a href="https://webbluetoothcg.github.io/web-bluetooth/#bluetooth-disableSimulation-command|bluetooth.disableSimulation">Here</a>}.
     *
     */
    public void disableEmulation();

    /**
     * Simulated preconnected Bluetooth Peripheral.
     * See {@link <a href="https://webbluetoothcg.github.io/web-bluetooth/#bluetooth-simulateconnectedperipheral-command|bluetooth.simulatePreconnectedPeripheral">Here</a>}.
     *
     * @param preconnectedPeripheral - The peripheral to simulate.
     *
     */
    public void simulatePreconnectedPeripheral(PreconnectedPeripheral preconnectedPeripheral);
}
