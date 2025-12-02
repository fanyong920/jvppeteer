package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.cdp.entities.DeviceRequestPromptDevice;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class DeviceRequestPrompt {
    /**
     * Current list of selectable devices.
     */
    protected List<DeviceRequestPromptDevice> devices = new ArrayList<>();
    /**
     * 等待设备请求提示设备
     * 该方法用于在一组设备中查找符合特定条件的设备如果在指定的超时时间内没有找到符合条件的设备，则返回null
     *
     * @param filter  用于筛选设备的函数，需要返回true以表示找到的设备符合条件
     * @param timeout 等待设备的超时时间（以毫秒为单位）如果为null，则使用默认超时时间
     * @return 返回找到的符合条件的设备，如果没有找到则返回
     */
    public abstract DeviceRequestPromptDevice waitForDevice(Function<DeviceRequestPromptDevice, Boolean> filter,Integer timeout);
    /**
     * 在提示列表中选择一个设备。
     *
     * @param device 要选择的设备
     */
    public abstract void select(DeviceRequestPromptDevice device);
    /**
     * 取消设备请求提示
     * <p>
     * 此方法用于取消一个尚未处理的设备请求提示如果提示已经处理，则不允许取消
     * 通过调用此方法，会向CDP会话发送取消请求，并更新当前请求的状态
     */
    public abstract void cancel();
}
