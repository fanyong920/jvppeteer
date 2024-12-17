package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class SupportedWebDriverCapabilities {

    private SupportedWebDriverCapability alwaysMatch;
    private List<SupportedWebDriverCapability> firstMatch;

    public SupportedWebDriverCapability getAlwaysMatch() {
        return alwaysMatch;
    }

    public void setAlwaysMatch(SupportedWebDriverCapability alwaysMatch) {
        this.alwaysMatch = alwaysMatch;
    }

    public List<SupportedWebDriverCapability> getFirstMatch() {
        return firstMatch;
    }

    public void setFirstMatch(List<SupportedWebDriverCapability> firstMatch) {
        this.firstMatch = firstMatch;
    }

    @Override
    public String toString() {
        return "SupportedWebDriverCapabilities{" +
                "alwaysMatch=" + alwaysMatch +
                ", firstMatch=" + firstMatch +
                '}';
    }
}
