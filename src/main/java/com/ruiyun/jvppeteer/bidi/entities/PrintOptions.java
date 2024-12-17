package com.ruiyun.jvppeteer.bidi.entities;

import java.util.List;

public class PrintOptions {
    private boolean background;
    private PrintMarginParameters margin;
    private Orientation orientation;
    private PrintPageParameters page;
    private List<String> pageRanges;
    private double scale = 1.0;
    private boolean shrinkToFit;

    public PrintOptions() {
    }

    public PrintOptions(boolean background, PrintMarginParameters margin, Orientation orientation, PrintPageParameters page, List<String> pageRanges, double scale, boolean shrinkToFit) {
        this.background = background;
        this.margin = margin;
        this.orientation = orientation;
        this.page = page;
        this.pageRanges = pageRanges;
        this.scale = scale;
        this.shrinkToFit = shrinkToFit;
    }

    public boolean getBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public boolean getShrinkToFit() {
        return shrinkToFit;
    }

    public void setShrinkToFit(boolean shrinkToFit) {
        this.shrinkToFit = shrinkToFit;
    }

    public List<String> getPageRanges() {
        return pageRanges;
    }

    public void setPageRanges(List<String> pageRanges) {
        this.pageRanges = pageRanges;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public PrintPageParameters getPage() {
        return page;
    }

    public void setPage(PrintPageParameters page) {
        this.page = page;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public PrintMarginParameters getMargin() {
        return margin;
    }

    public void setMargin(PrintMarginParameters margin) {
        this.margin = margin;
    }
}
