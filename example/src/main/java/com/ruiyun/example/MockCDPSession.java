package com.ruiyun.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.cdp.entities.DeviceRequestPromptDevice;
import com.ruiyun.jvppeteer.cdp.events.DeviceRequestPromptedEvent;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.DeviceRequestPromptManager;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class MockCDPSession extends CDPSession {
    //should respect timeout
    @Test
    public void test1() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt(1000);
        System.out.println(deviceRequestPrompt.getDevices().size());
    }

    //should respect default timeout when there is no custom timeout
    @Test
    public void test2() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(1);
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt();
        System.out.println(deviceRequestPrompt.getDevices().size());
    }

    //should prioritize exact timeout over default timeout
    @Test
    public void test3() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(0);
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt(1);
        System.out.println(deviceRequestPrompt.getDevices().size());
    }

    //should work with no timeout
    @Test
    public void test4() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(1500);
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(10000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt(0);
        System.out.println(deviceRequestPrompt.getDevices().size());
    }

    //should return the same prompt when there are many watchdogs simultaneously
    @Test
    public void test5() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(0);
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        AtomicReference<DeviceRequestPrompt> deviceRequestPrompt2 = new AtomicReference<>();
        new Thread(() -> {
            deviceRequestPrompt2.set(manager.waitForDevicePrompt());
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt();
        System.out.println(deviceRequestPrompt == deviceRequestPrompt2.get());
    }

    //should work with no timeout
    @Test
    public void test6() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPrompt deviceRequestPrompt = manager.waitForDevicePrompt(0);
        System.out.println(deviceRequestPrompt.getDevices().size());
    }

    //should listen and shortcut when there are no watchdogs
    @Test
    public void test7() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPromptManager manager = new DeviceRequestPromptManager(client, timeoutSettings);
        client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));

    }

    //DeviceRequestPrompt.devices
    @Test
    public void test8() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        try {
            System.out.println(deviceRequestPrompt.getDevices());
            List<DeviceRequestPromptDevice> devices = new ArrayList<>();
            devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
            Thread.sleep(2000);
            client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
            System.out.println(deviceRequestPrompt.getDevices());
            Thread.sleep(2000);
            List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
            devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
            devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
            client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
            System.out.println(deviceRequestPrompt.getDevices());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //does not list devices from events of another prompt
    @Test
    public void test9() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        try {
            System.out.println(deviceRequestPrompt.getDevices());
            List<DeviceRequestPromptDevice> devices = new ArrayList<>();
            devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
            Thread.sleep(2000);
            client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("88888888888888888888888888888888", devices));
            System.out.println(deviceRequestPrompt.getDevices());
            Thread.sleep(2000);
            List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
            devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
            devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
            client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("88888888888888888888888888888888", devices2));
            System.out.println(deviceRequestPrompt.getDevices());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    //should return first matching device
    @Test
    public void test10() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                System.out.println(deviceRequestPrompt.getDevices());
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        System.out.println(deviceRequestPromptDevice1);
    }

    //should return first matching device from already known devices
    @Test
    public void test11() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
        devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
        devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        System.out.println(deviceRequestPromptDevice1);
    }

    //should return device in the devices list
    @Test
    public void test12() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                System.out.println(deviceRequestPrompt.getDevices());
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        System.out.println(deviceRequestPrompt.getDevices().contains(deviceRequestPromptDevice1));
    }

    //should respect timeout
    @Test
    public void test13() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"),1);
        System.out.println(deviceRequestPrompt.getDevices().contains(deviceRequestPromptDevice1));
    }

    //should respect default timeout when there is no custom timeout
    @Test
    public void test14() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(2);
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        System.out.println(deviceRequestPrompt.getDevices().contains(deviceRequestPromptDevice1));
    }

    //should prioritize exact timeout over default timeout
    @Test
    public void test15() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        timeoutSettings.setDefaultTimeout(2);
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"),3);
        System.out.println(deviceRequestPrompt.getDevices().contains(deviceRequestPromptDevice1));
    }

    //should work with no timeout
    @Test
    public void test16() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"),0);
        System.out.println(deviceRequestPrompt.getDevices().contains(deviceRequestPromptDevice1));
    }

    //should return same device from multiple watchdogs
    @Test
    public void test17() throws InterruptedException {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        AtomicReference<DeviceRequestPromptDevice> deviceRequestPromptDevice1 = new AtomicReference<>();
        new Thread(() -> {
            deviceRequestPromptDevice1.set(deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1")));
            System.out.println("接收到了");
        }).start();

        DeviceRequestPromptDevice deviceRequestPromptDevice2 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        Thread.sleep(2000);
        System.out.println(deviceRequestPromptDevice2.equals(deviceRequestPromptDevice1.get()));
    }

    //should succeed with listed device
    @Test
    public void test18() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        deviceRequestPrompt.select(deviceRequestPromptDevice1);
    }

    //should error for device not listed in devices
    @Test
    public void test19() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        deviceRequestPrompt.select(new DeviceRequestPromptDevice("11111111","Device 1"));
    }

    //should fail when selecting prompt twice
    @Test
    public void test21() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        new Thread(() -> {
            try {
                List<DeviceRequestPromptDevice> devices = new ArrayList<>();
                devices.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                Thread.sleep(2000);
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices));
                System.out.println(deviceRequestPrompt.getDevices());
                Thread.sleep(2000);
                List<DeviceRequestPromptDevice> devices2 = new ArrayList<>();
                devices2.add(new DeviceRequestPromptDevice("00000000", "Device 0"));
                devices2.add(new DeviceRequestPromptDevice("11111111", "Device 1"));
                client.emit(ConnectionEvents.DeviceAccess_deviceRequestPrompted, new DeviceRequestPromptedEvent("00000000000000000000000000000000", devices2));
                System.out.println(deviceRequestPrompt.getDevices());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        DeviceRequestPromptDevice deviceRequestPromptDevice1 = deviceRequestPrompt.waitForDevice(deviceRequestPromptDevice -> deviceRequestPromptDevice.getName().equals("Device 1"));
        deviceRequestPrompt.select(deviceRequestPromptDevice1);
        deviceRequestPrompt.select(deviceRequestPromptDevice1);
    }

    //should succeed on first call
    @Test
    public void test22() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        deviceRequestPrompt.cancel();
    }

    //should fail when canceling prompt twice
    @Test
    public void test23() {
        MockCDPSession client = new MockCDPSession();
        TimeoutSettings timeoutSettings = new TimeoutSettings();
        DeviceRequestPrompt deviceRequestPrompt = new DeviceRequestPrompt(client, timeoutSettings, new DeviceRequestPromptedEvent("00000000000000000000000000000000", new ArrayList<>()));
        deviceRequestPrompt.cancel();
        deviceRequestPrompt.cancel();
    }

    @Override
    public Connection connection() {
        return null;
    }

    @Override
    public String id() {
        return "";
    }

    @Override
    public void detach() {

    }

    @Override
    public void onClosed() {

    }

    @Override
    public JsonNode send(String method, Object params, Integer timeout, boolean isBlocking) {
        return null;
    }
}
