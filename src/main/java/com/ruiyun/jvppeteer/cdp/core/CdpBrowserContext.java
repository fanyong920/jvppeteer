package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.BrowserContext;
import com.ruiyun.jvppeteer.api.core.Connection;
import com.ruiyun.jvppeteer.api.core.Page;
import com.ruiyun.jvppeteer.api.core.Target;
import com.ruiyun.jvppeteer.cdp.entities.CookieData;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.CreatePageOptions;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.common.Permission;
import com.ruiyun.jvppeteer.common.WebPermission;
import com.ruiyun.jvppeteer.cdp.entities.Cookie;
import com.ruiyun.jvppeteer.cdp.entities.CookieParam;
import com.ruiyun.jvppeteer.cdp.entities.TargetType;
import com.ruiyun.jvppeteer.util.Helper;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


import static com.ruiyun.jvppeteer.common.Constant.WEB_PERMISSION_TO_PROTOCOL_PERMISSION;

/**
 * BrowserContext 代表 browser 中的各个用户上下文。
 * <p>
 * 启动 browser 时，它至少有一个默认 浏览器上下文。其他可以使用 Browser.createBrowserContext() 创建。每个上下文都有独立的存储（cookies/localStorage/等）
 * <p>
 * BrowserContext emits 各种事件记录在 BrowserContextEvent 枚举中。
 * <p>
 * 如果 page 打开另一个 page，例如 使用 window.open，弹出窗口将属于父 页面的浏览器上下文。
 * <p>
 * 在 Chrome 中，所有非默认上下文都是隐身的，如果在启动浏览器时提供 --incognito 参数，默认浏览器上下文 可能会隐身。
 * <p>
 * 此类的构造函数被标记为内部构造函数。第三方代码不应直接调用构造函数或创建扩展 BrowserContext 类的子类。
 */
public class CdpBrowserContext extends BrowserContext {
    /**
     * 浏览器对应的websocket client包装类，用于发送和接受消息
     */
    private Connection connection;
    /**
     * 浏览器上下文对应的浏览器，一个上下文只有一个浏览器，但是一个浏览器可能有多个上下文
     */
    private CdpBrowser cdpBrowser;
    /**
     * 浏览器上下文id
     */
    private String id;

    public CdpBrowserContext() {
        super();
    }

    public CdpBrowserContext(Connection connection, CdpBrowser cdpBrowser, String contextId) {
        super();
        this.connection = connection;
        this.cdpBrowser = cdpBrowser;
        this.id = contextId;
    }


    public List<Target> targets() {
        return this.cdpBrowser.targets().stream().filter(target -> target.browserContext() == this).collect(Collectors.toList());
    }

    public List<Page> pages(boolean includeAll) {
        return this.targets().stream().filter(target -> TargetType.PAGE.equals(target.type()) || ((TargetType.OTHER.equals(target.type()) || includeAll) && this.cdpBrowser.getIsPageTargetCallback() != null ? this.cdpBrowser.getIsPageTargetCallback().apply(target) : true)).map(Target::page).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public void overridePermissions(String origin, WebPermission... webPermissions) {
        List<String> protocolPermissions = new ArrayList<>();
        if (webPermissions != null) {
            for (WebPermission permission : webPermissions) {
                String protocolPermission = WEB_PERMISSION_TO_PROTOCOL_PERMISSION.get(permission);
                ValidateUtil.assertArg(protocolPermission != null, "Unknown permission: " + permission);
                protocolPermissions.add(protocolPermission);
            }
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("origin", origin);
        if (StringUtil.isNotEmpty(this.id)) {
            params.put("browserContextId", this.id);
        }
        params.put("permissions", protocolPermissions);
        this.connection.send("Browser.grantPermissions", params);
    }

    public void clearPermissionOverrides() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("browserContextId", this.id);
        this.connection.send("Browser.resetPermissions", params);
    }

    public Page newPage(CreatePageOptions options) {
        synchronized (this) {
            return this.cdpBrowser.createPageInContext(this.id,options);
        }
    }

    public void close() {
        ValidateUtil.assertArg(StringUtil.isNotEmpty(this.id), "Default BrowserContext cannot be closed!");
        this.cdpBrowser.disposeContext(this.id);
    }

    @Override
    public List<Cookie> cookies() {
        Map<String, Object> params = ParamsFactory.create();
        if(StringUtil.isNotEmpty(this.id)){
            params.put("browserContextId", this.id);
        }
        JsonNode cookies = this.connection.send("Storage.getCookies", params).get("cookies");
        Iterator<JsonNode> elements = cookies.elements();
        List<Cookie> cookieList = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode cookie = elements.next();
            Cookie convertCookie = Constant.OBJECTMAPPER.convertValue(cookie, Cookie.class);
            JsonNode partitionKey = cookie.path("partitionKey");
            if (!partitionKey.isMissingNode()) {
                ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
                objectNode.put("sourceOrigin", partitionKey.get("topLevelSite").asText());
                objectNode.put("hasCrossSiteAncestor", partitionKey.get("hasCrossSiteAncestor").asBoolean());
                convertCookie.setPartitionKey(objectNode);
            }
            JsonNode sameParty = cookie.path("sameParty");
            if (!sameParty.isMissingNode()) {
                convertCookie.setSameParty(sameParty.asBoolean());
            } else {
                convertCookie.setSameParty(false);
            }
            cookieList.add(convertCookie);
        }
        return cookieList;
    }

    @Override
    public void setCookie(CookieData... cookies) {
        if (Objects.isNull(cookies)) {
            return;
        }
        for (CookieData cookie : cookies) {
            cookie.setPartitionKey(Helper.convertCookiesPartitionKeyFromPuppeteerToCdp(cookie.getPartitionKey()));
            cookie.setSameSite(Helper.convertSameSiteFromPuppeteerToCdp(cookie.getSameSite()));
        }

        Map<String, Object> params = ParamsFactory.create();
        params.put("cookies", cookies);
        if(StringUtil.isNotEmpty(this.id)){
            params.put("browserContextId", this.id);
        }
        this.connection.send("Storage.setCookies", params);
    }

    public CdpBrowser browser() {
        return cdpBrowser;
    }

    public String id() {
        return this.id;
    }

    @Override
    public void setPermission(String origin, List<Permission> permissions) {
        for (Permission permission : permissions) {
            // 准备发送给浏览器的参数
            Map<String, Object> params = ParamsFactory.create();
            params.put("origin", "*".equals(origin) ? null : origin);
            params.put("browserContextId", StringUtil.isEmpty(this.id) ? null : this.id);
            params.put("permission", permission.getPermission());
            params.put("setting", permission.getState());

            // 同步发送协议命令
            this.connection.send("Browser.setPermission", params);
        }
    }


}
