package com.ruiyun.jvppeteer.options;

import com.ruiyun.jvppeteer.core.page.PaperFormats;
import com.ruiyun.jvppeteer.protocol.DOM.Margin;

/**
 * 生成pdf时候需要的参数
 * 2020-05-02
 *
 * @author fanyong
 */
public class PDFOptions {
    /**
     *
     * 缩放网页的渲染。金额必须介于 0.1 和 2 之间。
     */
    public double scale = 1.00;
    /**
     * 是否显示页眉和页脚。
     */
    public boolean displayHeaderFooter = false;
    /**
     *打印标题的 HTML 模板。应该是有效的 HTML，其中包含用于向其中注入值的以下类：
     * <p>date 格式的打印日期</p>
     * <p>title 文件标题</p>
     * <p>url 文档位置</p>
     * <p>pageNumber 当前页码</p>
     * <p>文档总页数 totalPages</p>
     */
    public String headerTemplate = "";
    /**
     * 打印页脚的 HTML 模板。对特殊类具有与 PDFOptions.headerTemplate 相同的约束和支持。
     */
    public String footerTemplate = "";
    /**
     * 设置为 true 以打印背景图形。
     */
    public boolean printBackground = false;
    /**
     * 是否横向打印。
     */
    public boolean landscape = false;

    /**
     * 要打印的纸张范围，例如 1-5, 8, 11-13。
     * 空字符串，表示打印所有页面。
     */
    public String pageRanges = "";
    /**
     * 如果设置，则该选项优先于 width 和 height 选项
     */
    public PaperFormats format ;
    /**
     * 设置纸张宽度。你可以传入一个数字或带有单位的字符串
     */
    public String width;
    /**
     * 设置纸张的高度。你可以传入一个数字或带有单位的字符串。
     */
    public String height;
    /**
     * 使页面中声明的任何 CSS @page 大小优先于 width 或 height 或 format 选项中声明的大小。
     * false，它将缩放内容以适合纸张尺寸。
     */
    public boolean preferCSSPageSize;
    /**
     * 设置 PDF 页边距。
     */
    public PDFMargin margin = new PDFMargin();
    /**
     * 文件保存的路径。
     * 如果路径是相对路径，则相对于当前工作目录进行解析。
     */
    public String path;
    /**
     * 隐藏默认的白色背景并允许生成具有透明度的 pdf。
     */
    public boolean omitBackground = false;
    /**
     * （实验性）生成文档大纲。
     */
    public boolean outline = false;
    /**
     * 实验性）生成带标签的（可访问的）PDF。
     */
    public boolean tagged = true;
    /**
     * 超时（以毫秒为单位）。通过 0 禁用超时。
     * 可以使用 Page.setDefaultTimeout() 更改默认值
     */
    public int timeout = 30000;
    /**
     * 如果为真，则等待 document.fonts.ready 解析。如果页面在后台，则可能需要使用 Page.bringToFront() 激活页面。
     */
    public boolean waitForFonts = true;
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

    public PaperFormats getFormat() {
        return format;
    }

    public void setFormat(PaperFormats format) {
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

    public PDFMargin getMargin() {
        return margin;
    }

    public void setMargin(PDFMargin margin) {
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

    public boolean getOmitBackground() {
        return omitBackground;
    }

    public void setOmitBackground(boolean omitBackground) {
        this.omitBackground = omitBackground;
    }

    public boolean getOutline() {
        return outline;
    }

    public void setOutline(boolean outline) {
        this.outline = outline;
    }
    public boolean getTagged() {
        return tagged;
    }
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }
    public int getTimeout() {
        return timeout;
    }
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    public boolean getWaitForFonts() {
        return waitForFonts;
    }
    public void setWaitForFonts(boolean waitForFonts) {
        this.waitForFonts = waitForFonts;
    }
}
