package com.ruiyun.jvppeteer.cdp.core;

import com.ruiyun.jvppeteer.api.core.Extension;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.WebWorker;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.TargetCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CdpExtension extends Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(CdpExtension.class);
    private final CdpBrowser browser;

    public CdpExtension(String id, String version, String name, String path, boolean enabled, CdpBrowser browser) {
        super(id, version, name, path, enabled);
        this.browser = browser;
    }

    @Override
    public List<WebWorker> workers() {
        List<CdpTarget> targets = this.browser.targets();

        List<CdpTarget> extensionWorkers = targets.stream().filter((CdpTarget target) -> {
            String targetUrl = target.url();
            return (
                    target.type() == TargetType.SERVICE_WORKER &&
                            targetUrl.startsWith("chrome-extension://" + this.getId())
            );
        }).collect(Collectors.toList());
        List<WebWorker> workers = new ArrayList<>();
        for (CdpTarget target : extensionWorkers) {
            try {
                WebWorker worker = target.worker();
                if (worker != null) {
                    workers.add(worker);
                }
            } catch (Exception err) {
                if (canIgnoreError(err)) {
                    LOGGER.error("Ignoring non-fatal error while resolving extension worker", err);
                    continue;
                }
                throw err;
            }
        }

        return workers;
    }

    @Override
    public List<Page> pages() {
        List<CdpTarget> targets = this.browser.targets();
        List<CdpTarget> extensionPages = targets.stream().filter((CdpTarget target) -> {
            String targetUrl = target.url();
            return (
                    (target.type() == TargetType.PAGE || target.type() == TargetType.BACKGROUND_PAGE) &&
                            targetUrl.startsWith("chrome-extension://" + this.getId())
            );
        }).collect(Collectors.toList());
        List<Page> pages = new ArrayList<>();
        for (CdpTarget target : extensionPages) {
            try {
                Page page = target.asPage();
                if (page != null) {
                    pages.add(page);
                }
            } catch (Exception e) {
                if (canIgnoreError(e)) {
                    LOGGER.error("Ignoring non-fatal error while resolving extension page", e);
                    return null;
                }
                throw e;
            }
        }
        return pages;
    }

    @Override
    public void triggerAction(Page page) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("id", this.getId());
        params.put("targetId", page.tabId);
        this.browser.connection.send("Extensions.triggerAction", params);
    }

    private boolean canIgnoreError(Throwable error) {
        if (error == null) {
            return false;
        }
        String message = error.getMessage();
        if (message == null) {
            return false;
        }
        // isTargetClosedError 语义：连接/目标已关闭
        return error instanceof TargetCloseException
                || message.contains("No target with given id found");

    }
}
