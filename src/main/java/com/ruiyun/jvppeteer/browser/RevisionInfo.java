package com.ruiyun.jvppeteer.browser;

public class RevisionInfo {
	
	private String revision; 
	
	private String executablePath;
	
	private String folderPath;
	
	private boolean local;
	
	private String url;

	private String product;

	public RevisionInfo() {
	}

	public RevisionInfo(String revision, String executablePath, String folderPath, boolean local, String url, String product) {
		this.revision = revision;
		this.executablePath = executablePath;
		this.folderPath = folderPath;
		this.local = local;
		this.url = url;
		this.product = product;
	}

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

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}
}
