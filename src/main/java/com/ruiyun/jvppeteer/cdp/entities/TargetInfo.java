package com.ruiyun.jvppeteer.cdp.entities;

public class TargetInfo {

	private String targetId;
	private String type;
	private String title;
	private String url;
	private Boolean attached;
	private String openerId;
	private boolean canAccessOpener;
	private String openerFrameId;
	private String browserContextId;
	private String subtype;

	public String getTargetId() {
		return targetId;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Boolean getAttached() {
		return attached;
	}

	public void setAttached(Boolean attached) {
		this.attached = attached;
	}

	public String getOpenerId() {
		return openerId;
	}

	public void setOpenerId(String openerId) {
		this.openerId = openerId;
	}

	public String getBrowserContextId() {
		return browserContextId;
	}

	public void setBrowserContextId(String browserContextId) {
		this.browserContextId = browserContextId;
	}

	public String getSubtype() {
		return subtype;
	}
	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	public String getOpenerFrameId() {
		return openerFrameId;
	}
	public void setOpenerFrameId(String openerFrameId) {
		this.openerFrameId = openerFrameId;
	}
	public boolean getCanAccessOpener() {
		return canAccessOpener;
	}
	public void setCanAccessOpener(boolean canAccessOpener) {
		this.canAccessOpener = canAccessOpener;
	}

	@Override
	public String toString() {
		return "TargetInfo{" +
				"targetId='" + targetId + '\'' +
				", type='" + type + '\'' +
				", title='" + title + '\'' +
				", url='" + url + '\'' +
				", attached=" + attached +
				", openerId='" + openerId + '\'' +
				", canAccessOpener=" + canAccessOpener +
				", openerFrameId='" + openerFrameId + '\'' +
				", browserContextId='" + browserContextId + '\'' +
				", subtype='" + subtype + '\'' +
				'}';
	}
}
