package com.ruiyun.jvppeteer.options;

public class Viewport {
	
	/**
	 * 页面宽度像素
	 * <br/>
     * page width in pixels.
     */
    private Double width = 800.00;
    /**
     * 页面高度像素
     * <br/>
     * page height in pixels.
     */
    private Double height = 600.00;
    /**
     * 设置设备的缩放（可以认为是 dpr）。默认是 1
     * <br/>
     * Specify device scale factor (can be thought of as dpr).
     * @default 1
     */
    private Number deviceScaleFactor = 1;
    /**
     * 是否在页面中设置了 meta viewport 标签。默认是 false
     * Whether the meta viewport tag is taken into account.
     * @default false
     */
    private boolean isMobile;
    /**
     * 指定viewport是否支持触摸事件。默认是 false。
     * <br/>
     * Specifies if viewport supports touch events.
     * @default false
     */
    private boolean hasTouch ;
    /**
     * 指定视口是否处于横向模式。默认是 false。
     * <br/>
     * Specifies if viewport is in landscape mode.
     * @default false
     */
    private boolean isLandscape;

	public Viewport() {
	}

	public Viewport(Double width, Double height, Number deviceScaleFactor, boolean isMobile, boolean hasTouch, boolean isLandscape) {
		this.width = width;
		this.height = height;
		this.deviceScaleFactor = deviceScaleFactor;
		this.isMobile = isMobile;
		this.hasTouch = hasTouch;
		this.isLandscape = isLandscape;
	}

	public Double getWidth() {
		return width;
	}
	
	public void setWidth(Double width) {
		this.width = width;
	}
	
	public Double getHeight() {
		return height;
	}
	
	public void setHeight(Double height) {
		this.height = height;
	}
	
	public Number getDeviceScaleFactor() {
		return deviceScaleFactor;
	}
	
	public void setDeviceScaleFactor(Number deviceScaleFactor) {
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

    
}
