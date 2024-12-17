package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.cdp.entities.TargetInfo;
import java.util.List;
import java.util.Map;

public abstract class TargetManager extends EventEmitter<TargetManager.TargetManagerEvent> {
    public abstract Map<String, CdpTarget> getAvailableTargets();
    public abstract List<CdpTarget> getChildTargets(CdpTarget target);
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
        CdpTarget create(TargetInfo targetInfo, CDPSession session, CDPSession parentSession);
    }

}
