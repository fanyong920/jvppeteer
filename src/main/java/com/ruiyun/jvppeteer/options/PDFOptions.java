package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.protocol.DOM.Margin;

/**
 * 生成pdf时候需要的参数
 * 2020-05-02
 *
 * @author fanyong
 */
public class PDFOptions {

    private double scale = 1.00;

    private boolean displayHeaderFooter = false;

    private String headerTemplate = "";

    private String footerTemplate = "";

    private boolean printBackground = false;

    private boolean landscape = false;

    private String pageRanges = "";

    private String format ;

    private String width;

    private String height;

    private boolean preferCSSPageSize;

    private Margin margin = new Margin();

    private String path;

    public PDFOptions() {
        super();
    }

    public PDFOptions(String path) {
        this.path = path;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public String getHeaderTemplate() {
        return headerTemplate;
    }

    public void setHeaderTemplate(String headerTemplate) {
        this.headerTemplate = headerTemplate;
    }

    public String getFooterTemplate() {
        return footerTemplate;
    }

    public void setFooterTemplate(String footerTemplate) {
        this.footerTemplate = footerTemplate;
    }

    public String getPageRanges() {
        return pageRanges;
    }

    public void setPageRanges(String pageRanges) {
        this.pageRanges = pageRanges;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public Margin getMargin() {
        return margin;
    }

    public void setMargin(Margin margin) {
        this.margin = margin;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean getPreferCSSPageSize() {
        return preferCSSPageSize;
    }

    public void setPreferCSSPageSize(boolean preferCSSPageSize) {
        this.preferCSSPageSize = preferCSSPageSize;
    }

    public boolean getLandscape() {
        return landscape;
    }

    public void setLandscape(boolean landscape) {
        this.landscape = landscape;
    }

    public boolean getPrintBackground() {
        return printBackground;
    }

    public void setPrintBackground(boolean printBackground) {
        this.printBackground = printBackground;
    }

    public boolean getDisplayHeaderFooter() {
        return displayHeaderFooter;
    }

    public void setDisplayHeaderFooter(boolean displayHeaderFooter) {
        this.displayHeaderFooter = displayHeaderFooter;
    }
}
