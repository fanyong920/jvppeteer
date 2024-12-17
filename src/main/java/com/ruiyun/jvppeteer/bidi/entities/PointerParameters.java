package com.ruiyun.jvppeteer.bidi.entities;

public class PointerParameters {
    private PointerType pointerType;

    public PointerParameters(PointerType pointerType) {
        this.pointerType = pointerType;
    }

    public PointerType getPointerType() {
        return pointerType;
    }

    public void setPointerType(PointerType pointerType) {
        this.pointerType = pointerType;
    }

    public PointerParameters() {
    }

    @Override
    public String toString() {
        return "PointerParameters{" +
                "pointerType=" + pointerType +
                '}';
    }
}
