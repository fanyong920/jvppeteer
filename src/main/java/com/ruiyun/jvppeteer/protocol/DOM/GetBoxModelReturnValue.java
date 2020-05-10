package com.ruiyun.jvppeteer.protocol.DOM;

import com.ruiyun.jvppeteer.protocol.input.BoxModel;

public class GetBoxModelReturnValue {
     /**
     * Box model for the node.
     */
     private BoxModel model;

    public BoxModel getModel() {
        return model;
    }

    public void setModel(BoxModel model) {
        this.model = model;
    }
}
