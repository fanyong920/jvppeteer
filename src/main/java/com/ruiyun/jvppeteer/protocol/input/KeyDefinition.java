package com.ruiyun.jvppeteer.protocol.input;

public class KeyDefinition {

    private Number keyCode;

    private Number shiftKeyCode;

    private String key;

    private String shiftKey;

    private String code;

    private String text;

    private String shiftText;

    private Number location;

    public KeyDefinition() {
        super();
    }

    public KeyDefinition(Number keyCode, Number shiftKeyCode, String key, String code, String shiftKey, Number location) {
        this.keyCode = keyCode;
        this.shiftKeyCode = shiftKeyCode;
        this.key = key;
        this.shiftKey = shiftKey;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(Number keyCode, String key, String code) {
        super();
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
    }

    public KeyDefinition(String key, String code) {
        super();
        this.key = key;
        this.code = code;
    }

    public KeyDefinition(Number keyCode, String key, String code, Number location) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(String key, String code, Number location) {
        this.key = key;
        this.code = code;
        this.location = location;
    }

    public KeyDefinition(Number keyCode, String key) {
        this.keyCode = keyCode;
        this.key = key;
    }

    public KeyDefinition(Number keyCode, String code,  String shiftKey,String key) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.shiftKey = shiftKey;
    }

    public KeyDefinition(Number keyCode, String code, String key, String text, Number location) {
        this.keyCode = keyCode;
        this.key = key;
        this.code = code;
        this.text = text;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Number getShiftKeyCode() {
        return shiftKeyCode;
    }

    public void setShiftKeyCode(Number shiftKeyCode) {
        this.shiftKeyCode = shiftKeyCode;
    }

    public String getShiftKey() {
        return shiftKey;
    }

    public void setShiftKey(String shiftKey) {
        this.shiftKey = shiftKey;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getShiftText() {
        return shiftText;
    }

    public void setShiftText(String shiftText) {
        this.shiftText = shiftText;
    }

    public Number getLocation() {
        return location;
    }

    public void setLocation(Number location) {
        this.location = location;
    }
}
