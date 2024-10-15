package com.ruiyun.jvppeteer.entities;

public class CookiePartitionKey {
    private String topLevelSite;
    private boolean hasCrossSiteAncestor;

    public String getTopLevelSite() {
        return topLevelSite;
    }

    public void setTopLevelSite(String topLevelSite) {
        this.topLevelSite = topLevelSite;
    }

    public boolean getHasCrossSiteAncestor() {
        return hasCrossSiteAncestor;
    }

    public void setHasCrossSiteAncestor(boolean hasCrossSiteAncestor) {
        this.hasCrossSiteAncestor = hasCrossSiteAncestor;
    }

    @Override
    public String toString() {
        return "CookiePartitionKey{" +
                "topLevelSite='" + topLevelSite + '\'' +
                ", hasCrossSiteAncestor=" + hasCrossSiteAncestor +
                '}';
    }
}
