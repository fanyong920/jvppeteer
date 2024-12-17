package com.ruiyun.jvppeteer.cdp.entities;

public class ConsoleMessageLocation {
    /**
     * 资源的 URL（如果已知）或 undefined。
     */
    private String url;
    /**
     * 资源中从 0 开始的行号（如果已知）或 undefined。
     */
    private int lineNumber;
    /**
     * 资源中从 0 开始的列号（如果已知）或 undefined。
     */
    private int columnNumber;

    public ConsoleMessageLocation() {
        super();
    }

    public ConsoleMessageLocation(String url, int lineNumber) {
        super();
        this.url = url;
        this.lineNumber = lineNumber;
    }

    public ConsoleMessageLocation(String url, int lineNumber, int columnNumber) {
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

    @Override
    public String toString() {
        return "Location{" +
                "url='" + url + '\'' +
                ", lineNumber=" + lineNumber +
                ", columnNumber=" + columnNumber +
                '}';
    }
}
