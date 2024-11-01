package com.ruiyun.jvppeteer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DownloadState {
    @JsonProperty("inProgress")
    InProgress,
    @JsonProperty("completed")
    Completed,
    @JsonProperty("canceled")
    Canceled
}
