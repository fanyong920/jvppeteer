package com.ruiyun.jvppeteer.api.core;

import com.ruiyun.jvppeteer.cdp.entities.ClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.DragData;
import com.ruiyun.jvppeteer.cdp.entities.MouseClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseMoveOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseOptions;
import com.ruiyun.jvppeteer.cdp.entities.MouseWheelOptions;
import com.ruiyun.jvppeteer.cdp.entities.Point;

public abstract class Mouse {
    public Mouse() {
    }

    /**
     * 将鼠标重置为默认状态：没有按下任何按钮；位置在 (0,0)。
     */
    public abstract void reset();

    /**
     * 将鼠标移动到给定的坐标。
     *
     * @param x 鼠标的水平位置。
     * @param y 鼠标的垂直位置。
     */
    public void move(double x, double y) {
        this.move(x, y, new MouseMoveOptions());
    }

    /**
     * 将鼠标移动到给定的坐标。
     *
     * @param x       鼠标的水平位置。
     * @param y       鼠标的垂直位置。
     * @param options （可选的）配置行为的选项。
     */
    public abstract void move(double x, double y, MouseMoveOptions options);

    /**
     * 按下鼠标。
     */
    public void down() {
        this.down(new MouseOptions());
    }

    /**
     * 按下鼠标。
     *
     * @param options （可选的）配置行为的选项。
     */
    public abstract void down(MouseOptions options);

    /**
     * 释放鼠标。
     */
    public void up() {
        this.up(new ClickOptions());
    }

    /**
     * 释放鼠标。
     *
     * @param options （可选的）配置行为的选项。
     */
    public abstract void up(MouseOptions options);

    /**
     * mouse.move、mouse.down 和 mouse.up 的快捷方式。
     *
     * @param x 鼠标的水平位置。
     * @param y 鼠标的垂直位置。
     */
    public void click(double x, double y) {
        this.click(x, y, new ClickOptions());
    }

    /**
     * @param x       鼠标的水平位置。
     * @param y       鼠标的垂直位置。
     * @param options （可选的）配置行为的选项。
     */
    public abstract void click(double x, double y, MouseClickOptions options);

    /**
     * 触发一个鼠标滚轮事件
     *
     * @param options 选项
     */
    public abstract void wheel(MouseWheelOptions options);

    /**
     * 调度 drag 事件。
     *
     * @param start  拖动的起点
     * @param target 拖动到的点
     * @return DragData
     */
    public abstract DragData drag(Point start, Point target);

    /**
     * 调度 dragenter 事件。
     *
     * @param target 触发 dragenter 事件的点
     * @param data   拖动包含项目和操作蒙版的数据
     */
    public abstract void dragEnter(Point target, DragData data);

    /**
     * 调度 dragover 事件。
     *
     * @param target 触发 dragover 事件的点
     * @param data   拖动包含项目和操作蒙版的数据
     */
    public abstract void dragOver(Point target, DragData data);

    /**
     * 按顺序执行 dragenter、dragover 和 drop。
     *
     * @param target 点放在
     * @param data   拖动包含项目和操作蒙版的数据
     */
    public abstract void drop(Point target, DragData data);

    /**
     * 按顺序执行拖动、拖动、拖动和放置。
     *
     * @param start  拖动的点
     * @param target 点放在
     * @param delay  (可选的）选项的对象。接受延迟，如果指定，则为 dragover 和 drop 之间等待的时间（以毫秒为单位）。默认为 0。
     */
    public abstract void dragAndDrop(Point start, Point target, int delay) throws InterruptedException;
}
