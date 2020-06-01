package com.ruiyun.jvppeteer.protocol.log;

import com.ruiyun.jvppeteer.protocol.log.LogEntry;

/**
 * Issued when new message was logged.
 */
public class EntryAddedPayload {

    /**
     * The entry.
     */
    private LogEntry entry;

    public LogEntry getEntry() {
        return entry;
    }

    public void setEntry(LogEntry entry) {
        this.entry = entry;
    }

    @Override
    public String toString() {
        return "EntryAddedPayload{" +
                "entry=" + entry +
                '}';
    }
}
