package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;

public class SelectorParseResult {
    private List<JsonNode> selectors;
    private boolean isPureCSS;
    private boolean hasPseudoClasses;
    private boolean hasAria;

    public List<JsonNode> getSelectors() {
        return selectors;
    }

    public void setSelectors(List<JsonNode> selectors) {
        this.selectors = selectors;
    }

    public boolean getPureCSS() {
        return isPureCSS;
    }

    public void setPureCSS(boolean pureCSS) {
        isPureCSS = pureCSS;
    }

    public boolean getHasPseudoClasses() {
        return hasPseudoClasses;
    }

    public void setHasPseudoClasses(boolean hasPseudoClasses) {
        this.hasPseudoClasses = hasPseudoClasses;
    }

    public boolean getHasAria() {
        return hasAria;
    }

    public void setHasAria(boolean hasAria) {
        this.hasAria = hasAria;
    }

    @Override
    public String toString() {
        return "SelectorParseResult{" +
                "selectors=" + selectors +
                ", isPureCSS=" + isPureCSS +
                ", hasPseudoClasses=" + hasPseudoClasses +
                ", hasAria=" + hasAria +
                '}';
    }
}
