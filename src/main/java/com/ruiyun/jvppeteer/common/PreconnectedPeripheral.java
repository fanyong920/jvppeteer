package com.ruiyun.jvppeteer.common;

import java.util.List;

public class PreconnectedPeripheral {
    private String address;
    private String name;
    private List<BluetoothManufacturerData> manufacturerData;
    private List<String> knownServiceUuids;

    public PreconnectedPeripheral(String address, String name, List<BluetoothManufacturerData> manufacturerData, List<String> knownServiceUuids) {
        this.address = address;
        this.name = name;
        this.manufacturerData = manufacturerData;
        this.knownServiceUuids = knownServiceUuids;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<BluetoothManufacturerData> getManufacturerData() {
        return manufacturerData;
    }

    public void setManufacturerData(List<BluetoothManufacturerData> manufacturerData) {
        this.manufacturerData = manufacturerData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getKnownServiceUuids() {
        return knownServiceUuids;
    }

    public void setKnownServiceUuids(List<String> knownServiceUuids) {
        this.knownServiceUuids = knownServiceUuids;
    }

    @Override
    public String toString() {
        return "PreconnectedPeripheral{" +
                "address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", manufacturerData=" + manufacturerData +
                ", knownServiceUuids=" + knownServiceUuids +
                '}';
    }
}
