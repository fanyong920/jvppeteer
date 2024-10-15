package com.ruiyun.jvppeteer.entities;

public class DragDataItem {
    /**
     * Mime type of the dragged data.
     */
    private String mimeType;
    /**
     * Depending of the value of mimeType, it contains the dragged link, text, HTML markup or any other data.
     */
    private String data;
    /**
     * Title associated with a link. Only valid when mimeType == "text/ uri-list".
     */
    private String title;
    /**
     * Stores the base URL for the contained markup. Only valid when mimeType == "text/ html".
     */
    private String baseURL;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DragDataItem{" +
                "mimeType='" + mimeType + '\'' +
                ", data='" + data + '\'' +
                ", title='" + title + '\'' +
                ", baseURL='" + baseURL + '\'' +
                '}';
    }
}
