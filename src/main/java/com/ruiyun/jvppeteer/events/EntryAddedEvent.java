package com.ruiyun.jvppeteer.events;

import com.ruiyun.jvppeteer.entities.LogEntry;

/**
 * Issued when new message was logged.
 */
public class EntryAddedEvent {

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
