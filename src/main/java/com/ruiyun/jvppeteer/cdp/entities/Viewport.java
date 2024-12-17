package com.ruiyun.jvppeteer.cdp.entities;

public class Viewport {
	
	/**
	 * 页面宽度像素
	 * <br/>
     * page width in pixels.
     */
    private int width = 800;
    /**
     * 页面高度像素
     * <br/>
     * page height in pixels.
     */
    private int height = 600;
    /**
     * 设置设备的缩放（可以认为是 dpr）。默认是 1
     * <br/>
     * Specify device scale factor (can be thought of as dpr).
     *默认是  1
     */
    private Double deviceScaleFactor = 1.00;
    /**
     * 是否在页面中设置了 meta viewport 标签。默认是 false
     * Whether the meta viewport tag is taken into account.
     * 默认是  false
     */
    private boolean isMobile;
    /**
     * 指定viewport是否支持触摸事件。默认是 false。
     * <br/>
     * Specifies if viewport supports touch events.
     * 默认是  false
     */
    private boolean hasTouch ;
    /**
     * 指定视口是否处于横向模式。默认是 false。
     * <br/>
     * Specifies if viewport is in landscape mode.
     * 默认是  false
     */
    private boolean isLandscape;

	public Viewport() {
	}

	public Viewport(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public Viewport(int width, int height, Double deviceScaleFactor, boolean isMobile, boolean hasTouch, boolean isLandscape) {
		this.width = width;
		this.height = height;
		this.deviceScaleFactor = deviceScaleFactor;
		this.isMobile = isMobile;
		this.hasTouch = hasTouch;
		this.isLandscape = isLandscape;
	}

	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public Double getDeviceScaleFactor() {
		return deviceScaleFactor;
	}
	
	public void setDeviceScaleFactor(Double deviceScaleFactor) {
		this.deviceScaleFactor = deviceScaleFactor;
	}
	
	public boolean getIsMobile() {
		return isMobile;
	}
	
	public void setIsMobile(boolean isMobile) {
		this.isMobile = isMobile;
	}
	
	public boolean getHasTouch() {
		return hasTouch;
	}
	
	public void setHasTouch(boolean hasTouch) {
		this.hasTouch = hasTouch;
	}
	
	public boolean getIsLandscape() {
		return isLandscape;
	}
	
	public void setIsLandscape(boolean isLandscape) {
		this.isLandscape = isLandscape;
	}

	@Override
	public String toString() {
		return "Viewport{" +
				"width=" + width +
				", height=" + height +
				", deviceScaleFactor=" + deviceScaleFactor +
				", isMobile=" + isMobile +
				", hasTouch=" + hasTouch +
				", isLandscape=" + isLandscape +
				'}';
	}
}
