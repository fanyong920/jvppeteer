package com.ruiyun.jvppeteer.protocol.CSS;

/**
 * CSS stylesheet metainformation.
 */
public class CSSStyleSheetHeader {

    /**
     * The stylesheet identifier.
     */
    private String styleSheetId;
    /**
     * Owner frame identifier.
     */
    private String frameId;
    /**
     * Stylesheet resource URL.
     */
    private String sourceURL;
    /**
     * URL of source map associated with the stylesheet (if any).
     */
    private String sourceMapURL;
    /**
     * Stylesheet origin.
     * /**
     *          *Stylesheet type: "injected" for stylesheets injected via extension, "user-agent" for user-agent
     * stylesheets, "inspector" for stylesheets created by the inspector (i.e. those holding the "via
     * inspector" rules), "regular" for regular stylesheets.
     *          "injected"|"user-agent"|"inspector"|"regular"
     */
    private String origin;
    /**
     * Stylesheet title.
     */
    private String title;
    /**
     * The backend id for the owner node of the stylesheet.
     */
    private Number ownerNode;
    /**
     * Denotes whether the stylesheet is disabled.
     */
   private boolean disabled;
    /**
     * Whether the sourceURL field value comes from the sourceURL comment.
     */
    private  boolean hasSourceURL;
    /**
     * Whether this stylesheet is created for STYLE tag by parser. This flag is not set for
     document.written STYLE tags.
     */
    private boolean isInline;
    /**
     * Line offset of the stylesheet within the resource (zero based).
     */
    private int startLine;
    /**
     * Column offset of the stylesheet within the resource (zero based).
     */
    private int startColumn;
    /**
     * Size of the content (in characters).
     */
    private int length;
    /**
     * Line offset of the end of the stylesheet within the resource (zero based).
     */
    private int endLine;
    /**
     * Column offset of the end of the stylesheet within the resource (zero based).
     */
    private int endColumn;

    public String getStyleSheetId() {
        return styleSheetId;
    }

    public void setStyleSheetId(String styleSheetId) {
        this.styleSheetId = styleSheetId;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    public String getSourceURL() {
        return sourceURL;
    }

    public void setSourceURL(String sourceURL) {
        this.sourceURL = sourceURL;
    }

    public String getSourceMapURL() {
        return sourceMapURL;
    }

    public void setSourceMapURL(String sourceMapURL) {
        this.sourceMapURL = sourceMapURL;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Number getOwnerNode() {
        return ownerNode;
    }

    public void setOwnerNode(Number ownerNode) {
        this.ownerNode = ownerNode;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public boolean getHasSourceURL() {
        return hasSourceURL;
    }

    public void setHasSourceURL(boolean hasSourceURL) {
        this.hasSourceURL = hasSourceURL;
    }

    public boolean getIsInline() {
        return isInline;
    }

    public void setInline(boolean inline) {
        isInline = inline;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getStartColumn() {
        return startColumn;
    }

    public void setStartColumn(int startColumn) {
        this.startColumn = startColumn;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(int endColumn) {
        this.endColumn = endColumn;
    }
}
