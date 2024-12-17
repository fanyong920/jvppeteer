package com.ruiyun.jvppeteer.cdp.events;

import com.ruiyun.jvppeteer.api.core.EventEmitter;

public class IsolatedWorldEmitter extends EventEmitter<IsolatedWorldEmitter.IsolatedWorldEventType> {

    public enum IsolatedWorldEventType {
        Context("context"),
        Disposed("disposed"),
        Consoleapicalled("consoleapicalled"),
        Bindingcalled("bindingcalled");
        private final String eventType;

        IsolatedWorldEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getEventType() {
            return eventType;
        }

    }
}
