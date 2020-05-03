package com.ruiyun.jvppeteer.protocol.page;
/**
 * Navigation history entry.
 */
public class NavigationEntry {
    /**
     * Unique id of the navigation history entry.
     */
    private int id;
    /**
     * URL of the navigation history entry.
     */
    private String url;
    /**
     * URL that the user typed in the url bar.
     */
    private String userTypedURL;
    /**
     * Title of the navigation history entry.
     */
    private String title;
    /**
     * Transition type.
     *"link"|"typed"|"address_bar"|"auto_bookmark"|"auto_subframe"|"manual_subframe"|"generated"|"auto_toplevel"|"form_submit"|"reload"|"keyword"|"keyword_generated"|"other";
     */
    private String transitionType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserTypedURL() {
        return userTypedURL;
    }

    public void setUserTypedURL(String userTypedURL) {
        this.userTypedURL = userTypedURL;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTransitionType() {
        return transitionType;
    }

    public void setTransitionType(String transitionType) {
        this.transitionType = transitionType;
    }
}
