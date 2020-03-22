package com.ruiyun.jvppeteer.protocol.target;

public class TargetInfo {
	
	private String targetId;

	private String type;

	private String title;

	private String url;

	private Boolean attached;

	private String openerId;

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
	  
	  
}
