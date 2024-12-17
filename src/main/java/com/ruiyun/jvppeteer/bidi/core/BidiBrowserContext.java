package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.EventEmitter;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.api.events.BrowserContextEvents;
import com.ruiyun.jvppeteer.api.events.PageEvents;
import com.ruiyun.jvppeteer.api.events.TrustedEmitter;
import com.ruiyun.jvppeteer.bidi.entities.BytesValue;
import com.ruiyun.jvppeteer.bidi.entities.CreateBrowsingContextOptions;
import com.ruiyun.jvppeteer.bidi.entities.CreateType;
import com.ruiyun.jvppeteer.bidi.entities.GetCookiesOptions;
import com.ruiyun.jvppeteer.bidi.entities.PartialCookie;
import com.ruiyun.jvppeteer.bidi.entities.PermissionOverride;
import com.ruiyun.jvppeteer.bidi.entities.PermissionState;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.Viewport;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.common.Constant.WEB_PERMISSION_TO_PROTOCOL_PERMISSION;
import static com.ruiyun.jvppeteer.util.Helper.bidiToPuppeteerCookie;
import static com.ruiyun.jvppeteer.util.Helper.convertCookiesPartitionKeyFromPuppeteerToBiDi;
import static com.ruiyun.jvppeteer.util.Helper.convertCookiesSameSiteCdpToBiDi;

public class BidiBrowserContext extends BrowserContext {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BidiBrowserContext.class);
    private final TrustedEmitter<BrowserContextEvents> trustedEmitter = new TrustedEmitter<>();
    private final BidiBrowser browser;
    private final Viewport defaultViewport;
    final UserContext userContext;
    private final Map<BrowsingContext, BidiPage> pages = new WeakHashMap<>();
    private final Map<BidiPage, Map<BidiPageTarget, Map<Object, Target>>> targets = new ConcurrentHashMap<>();
    private final List<PermissionOverride> overrides = new ArrayList<>();

    private BidiBrowserContext(BidiBrowser browser, UserContext userContext, Viewport defaultViewport) {
        super();
        this.browser = browser;
        this.userContext = userContext;
        this.defaultViewport = defaultViewport;
        this.trustedEmitter.pipeTo(this);
    }

    public static BidiBrowserContext from(BidiBrowser browser, UserContext userContext, Viewport defaultViewport) {
        BidiBrowserContext context = new BidiBrowserContext(browser, userContext, defaultViewport);
        context.initialize();
        return context;
    }

    private void initialize() {
        for (BrowsingContext browsingContext : this.userContext.browsingContexts()) {
            this.createPage(browsingContext);
        }
        this.userContext.on(UserContext.UserContextEvent.browsingcontext, (Consumer<BrowsingContext>) browsingcontext -> {
            BidiPage page = this.createPage(browsingcontext);
            browsingcontext.on(BrowsingContext.BrowsingContextEvents.DOMContentLoaded, ignored -> {
                if (Objects.nonNull(browsingcontext.originalOpener())) {
                    for (BrowsingContext context : this.userContext.browsingContexts()) {
                        if (browsingcontext.originalOpener().equals(context.id())) {
                            this.pages.get(context).trustedEmitter().emit(PageEvents.Popup, page);
                        }
                    }
                }
            });
        });
        this.userContext.on(UserContext.UserContextEvent.closed, ignored -> {
            this.trustedEmitter.removeAllListeners(null);
        });
    }

    private BidiPage createPage(BrowsingContext browsingContext) {
        BidiPage page = BidiPage.from(this, browsingContext);
        this.pages.put(browsingContext, page);
        page.trustedEmitter().on(PageEvents.Close, ignored -> {
            this.pages.remove(browsingContext);
        });
        // -- Target stuff starts here --
        BidiPageTarget pageTarget = new BidiPageTarget(page);
        Map<BidiPageTarget, Map<Object, Target>> bidiPages = new HashMap<>();
        Map<Object, Target> pageTargets = new HashMap<>();
        bidiPages.put(pageTarget, pageTargets);
        this.targets.put(page, bidiPages);

        page.trustedEmitter().on(PageEvents.FrameAttached, (Consumer<BidiFrame>) bidiFrame -> {
            BidiFrameTarget target = new BidiFrameTarget(bidiFrame);
            pageTargets.put(bidiFrame, target);
            this.trustedEmitter().emit(BrowserContextEvents.TargetCreated, target);
        });

        page.trustedEmitter().on(PageEvents.FrameNavigated, (Consumer<BidiFrame>) bidiFrame -> {
            BidiFrameTarget target = (BidiFrameTarget) pageTargets.get(bidiFrame);
            // If there is no target, then this is the page's frame.
            if (Objects.isNull(target)) {
                this.trustedEmitter.emit(BrowserContextEvents.TargetChanged, pageTarget);
            } else {
                this.trustedEmitter.emit(BrowserContextEvents.TargetChanged, target);
            }
        });

        page.trustedEmitter().on(PageEvents.FrameDetached, (Consumer<BidiFrame>) bidiFrame -> {
            BidiFrameTarget target = (BidiFrameTarget) pageTargets.get(bidiFrame);
            if (Objects.isNull(target)) {
                return;
            }
            pageTargets.remove(bidiFrame);
            this.trustedEmitter.emit(BrowserContextEvents.TargetDestroyed, target);
        });

        page.trustedEmitter().on(PageEvents.WorkerCreated, (Consumer<BidiWebWorker>) bidiWorker -> {
            BidiWorkerTarget target = new BidiWorkerTarget(bidiWorker);
            pageTargets.put(bidiWorker, target);
            this.trustedEmitter.emit(BrowserContextEvents.TargetCreated, target);
        });

        page.trustedEmitter().on(PageEvents.WorkerDestroyed, (Consumer<BidiWebWorker>) bidiWorker -> {
            BidiWorkerTarget target = (BidiWorkerTarget) pageTargets.get(bidiWorker);
            if (Objects.isNull(target)) {
                return;
            }
            pageTargets.remove(bidiWorker);
            this.trustedEmitter.emit(BrowserContextEvents.TargetDestroyed, target);
        });

        page.trustedEmitter().on(PageEvents.Close, (ignored) -> {
            this.targets.remove(page);
            this.trustedEmitter.emit(BrowserContextEvents.TargetDestroyed, pageTarget);
        });
        this.trustedEmitter.emit(BrowserContextEvents.TargetCreated, pageTarget);
        // -- Target stuff ends here --
        return page;
    }

    @Override
    public List<Target> targets() {
        return this.targets.values().stream().map(Map::entrySet).flatMap(entrySet -> {
            List<Target> targets = new ArrayList<>();
            for (Map.Entry<BidiPageTarget, Map<Object, Target>> entry : entrySet) {
                BidiPageTarget target = entry.getKey();
                targets.add(target);
                targets.addAll(entry.getValue().values());
            }
            return targets.stream();
        }).collect(Collectors.toList());
    }

    @Override
    public List<Page> pages() {
        return this.userContext.browsingContexts().stream().map(this.pages::get).collect(Collectors.toList());
    }

    @Override
    public void overridePermissions(String origin, WebPermission... webPermissions) {
        List<String> permissions = new ArrayList<>();
        if (Objects.nonNull(webPermissions)) {
            for (WebPermission permission : webPermissions) {
                if (!WEB_PERMISSION_TO_PROTOCOL_PERMISSION.containsKey(permission)) {
                    throw new JvppeteerException("Unknown permission: " + permission);
                }
                permissions.add(permission.getPermission());
            }
        }
        WEB_PERMISSION_TO_PROTOCOL_PERMISSION.keySet().forEach(permission -> {
            try {
                this.userContext.setPermissions(origin, permission.getPermission(), permissions.contains(permission.getPermission()) ? PermissionState.Granted : PermissionState.Denied);
                this.overrides.add(new PermissionOverride(origin, permission));
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        });
    }

    @Override
    public void clearPermissionOverrides() {
        for (PermissionOverride override : this.overrides) {
            try {
                this.userContext.setPermissions(override.getOrigin(), override.getPermission().getPermission(), PermissionState.Prompt);
            } catch (Exception e) {
                LOGGER.error("jvppeteer error", e);
            }
        }
        this.overrides.clear();
    }

    @Override
    public Page newPage() {
        synchronized (this) {
            BrowsingContext context = this.userContext.createBrowsingContext(CreateType.Tab, new CreateBrowsingContextOptions());
            Page page = this.pages.get(context);
            Objects.requireNonNull(page, "Page is not found");
            if (Objects.nonNull(this.defaultViewport)) {
                try {
                    page.setViewport(this.defaultViewport);
                } catch (Exception e) {
                    // No support for setViewport in Firefox.
                }
            }
            return page;
        }
    }

    @Override
    public BidiBrowser browser() {
        return this.browser;
    }

    @Override
    public void close() {
        ValidateUtil.assertArg(!Objects.equals(UserContext.DEFAULT, this.userContext.id()), "Default BrowserContext cannot be closed!");
        try {
            this.userContext.remove();
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
        }
        this.targets.clear();
    }

    @Override
    public List<Cookie> cookies() {
        List<JsonNode> cookies = this.userContext.getCookies(new GetCookiesOptions(), null);
        List<Cookie> result = new ArrayList<>();
        for (JsonNode cookie : cookies) {
            result.add(bidiToPuppeteerCookie(cookie));
        }
        return result;
    }

    @Override
    public void setCookie(CookieParam... cookies) {
        if (Objects.isNull(cookies)) {
            return;
        }
        for (CookieParam cookie : cookies) {
            PartialCookie bidiCookie = new PartialCookie();
            bidiCookie.setDomain(cookie.getDomain());
            bidiCookie.setName(cookie.getName());
            bidiCookie.setValue(new BytesValue("string", cookie.getValue()));
            bidiCookie.setPath(cookie.getPath());
            bidiCookie.setHttpOnly(cookie.getHttpOnly());
            bidiCookie.setSecure(cookie.getSecure());
            if (Objects.nonNull(cookie.getSameSite())) {
                bidiCookie.setSameSite(convertCookiesSameSiteCdpToBiDi(cookie.getSameSite()));
            }
            bidiCookie.setExpiry(cookie.getExpires());
            bidiCookie.setSameParty(cookie.getSameParty());
            bidiCookie.setSourceScheme(cookie.getSourceScheme());
            bidiCookie.setPriority(cookie.getPriority());
            bidiCookie.setUrl(cookie.getUrl());
            this.userContext.setCookie(bidiCookie, convertCookiesPartitionKeyFromPuppeteerToBiDi(cookie.getPartitionKey()));
        }
    }

    @Override
    public String id() {
        if (Objects.equals(UserContext.DEFAULT, this.userContext.id())) {
            return null;
        }
        return this.userContext.id();
    }

    EventEmitter<BrowserContextEvents> trustedEmitter() {
        return trustedEmitter;
    }


}
