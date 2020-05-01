package com.ruiyun.jvppeteer.types.page.payload;

import java.util.List;

public class GetNavigationHistoryReturnValue {

    /**
     * Index of the current navigation history entry.
     */
    private int currentIndex;
    /**
     * Array of navigation history entries.
     */
   private List<NavigationEntry> entries;

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public List<NavigationEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<NavigationEntry> entries) {
        this.entries = entries;
    }
}
