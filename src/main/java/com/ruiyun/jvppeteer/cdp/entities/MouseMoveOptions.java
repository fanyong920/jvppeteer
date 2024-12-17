package com.ruiyun.jvppeteer.cdp.entities;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class MouseMoveOptions {
    private Integer steps;
    //bidi 使用
    private ObjectNode origin;

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }


    public ObjectNode getOrigin() {
        return origin;
    }

    public void setOrigin(ObjectNode origin) {
        this.origin = origin;
    }
}
