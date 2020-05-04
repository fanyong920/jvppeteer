package com.ruiyun.jvppeteer.protocol.input;

public class KeyDescription {

    private Number keyCode;

    private String key;

    private String text;

    private String code;

    private int location;

    public KeyDescription() {
        super();
    }

    public KeyDescription( String key, Number keyCode, String code, String text, int location) {
        super();
        this.key = key;
        this.keyCode = keyCode;
        this.text = text;
        this.code = code;
        this.location = location;
    }

    public Number getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(Number keyCode) {
        this.keyCode = keyCode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }
}
