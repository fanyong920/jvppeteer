package com.ruiyun.jvppeteer.core;

import com.ruiyun.jvppeteer.core.page.Target;
import com.ruiyun.jvppeteer.core.page.TargetInfo;
import com.ruiyun.jvppeteer.events.EventEmitter;
import com.ruiyun.jvppeteer.transport.CDPSession;

import java.util.Map;

public abstract class TargetManager extends EventEmitter<TargetManager.TargetManagerEvent>  {
    public abstract Map<String, Target> getAvailableTargets();
    public abstract void initialize();
    public abstract void dispose();
    public enum TargetManagerEvent{

        TargetDiscovered("targetDiscovered"),
        TargetAvailable("targetAvailable"),
        TargetGone("targetGone"),
        /**
         * Emitted after a target has been initialized and whenever its URL changes.
         */
        TargetChanged("targetChanged");
        private String eventName;

        TargetManagerEvent(String eventName) {
            this.eventName = eventName;
        }

        public String getEventName() {
            return eventName;
        }

        public void setEventName(String eventName) {
            this.eventName = eventName;
        }
    }
    @FunctionalInterface
    public interface TargetFactory{
        Target create(TargetInfo targetInfo, CDPSession session,CDPSession parentSession);
    }

}
