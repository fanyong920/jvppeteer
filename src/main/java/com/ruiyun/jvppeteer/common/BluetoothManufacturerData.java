package com.ruiyun.jvppeteer.common;

public class BluetoothManufacturerData {
    private int key;
    private String data;

    public BluetoothManufacturerData(int key, String data) {
        this.key = key;
        this.data = data;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BluetoothManufacturerData{" +
                "key=" + key +
                ", data='" + data + '\'' +
                '}';
    }
}
