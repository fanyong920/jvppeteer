package com.ruiyun.jvppeteer.protocol.console;

public class Location {

    private String url;

    private int lineNumber;

    private int columnNumber;

    public Location() {
        super();
    }

    public Location(String url, int lineNumber) {
        super();
        this.url = url;
        this.lineNumber = lineNumber;
    }

    public Location(String url, int lineNumber, int columnNumber) {
        super();
        this.url = url;
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }
}
