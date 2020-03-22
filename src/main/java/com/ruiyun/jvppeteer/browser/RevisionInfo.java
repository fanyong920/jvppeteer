package com.ruiyun.jvppeteer.browser;

public class RevisionInfo {
	
	private String revision; 
	
	private String executablePath;
	
	private String folderPath;
	
	private boolean local;
	
	private String url;

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public String getExecutablePath() {
		return executablePath;
	}

	public void setExecutablePath(String executablePath) {
		this.executablePath = executablePath;
	}

	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

	public boolean getLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
