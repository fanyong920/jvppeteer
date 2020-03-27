package com.ruiyun.jvppeteer.events.definition;

public abstract class AbstractBrowserListener implements BrowserListener<BrowserEvent> {
	
	private String mothod;

	public String getMothod() {
		return mothod;
	}

	public void setMothod(String mothod) {
		this.mothod = mothod;
	}

	public AbstractBrowserListener(String mothod) {
		super();
		this.mothod = mothod;
	}
	
	
}
