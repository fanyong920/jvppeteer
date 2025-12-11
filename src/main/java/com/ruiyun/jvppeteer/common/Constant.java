package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruiyun.jvppeteer.api.events.ConnectionEvents;
import com.ruiyun.jvppeteer.bidi.entities.AuthRequiredParameters;
import com.ruiyun.jvppeteer.bidi.entities.BaseParameters;
import com.ruiyun.jvppeteer.bidi.entities.FetchErrorParameters;
import com.ruiyun.jvppeteer.bidi.entities.LogEntry;
import com.ruiyun.jvppeteer.bidi.entities.MessageParameters;
import com.ruiyun.jvppeteer.bidi.entities.RealmDestroyedParameters;
import com.ruiyun.jvppeteer.bidi.entities.RealmInfo;
import com.ruiyun.jvppeteer.bidi.entities.ResponseCompletedParameters;
import com.ruiyun.jvppeteer.bidi.entities.ResponseStartedParameters;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptClosedParameters;
import com.ruiyun.jvppeteer.bidi.entities.UserPromptOpenedParameters;
import com.ruiyun.jvppeteer.bidi.events.ClosedEvent;
import com.ruiyun.jvppeteer.bidi.events.ContextCreatedEvent;
import com.ruiyun.jvppeteer.bidi.events.FileDialogInfo;
import com.ruiyun.jvppeteer.bidi.events.NavigationInfoEvent;
import com.ruiyun.jvppeteer.cdp.entities.DragInterceptedEvent;
import com.ruiyun.jvppeteer.cdp.entities.RequestWillBeSentExtraInfoEvent;
import com.ruiyun.jvppeteer.cdp.events.AttachedToTargetEvent;
import com.ruiyun.jvppeteer.cdp.events.AuthRequiredEvent;
import com.ruiyun.jvppeteer.cdp.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.cdp.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.cdp.events.DetachedFromTargetEvent;
import com.ruiyun.jvppeteer.cdp.events.DeviceRequestPromptedEvent;
import com.ruiyun.jvppeteer.cdp.events.DownloadProgressEvent;
import com.ruiyun.jvppeteer.cdp.events.DownloadWillBeginEvent;
import com.ruiyun.jvppeteer.cdp.events.EntryAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.cdp.events.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameAttachedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameDetachedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameNavigatedEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameStartedLoadingEvent;
import com.ruiyun.jvppeteer.cdp.events.FrameStoppedLoadingEvent;
import com.ruiyun.jvppeteer.cdp.events.JavascriptDialogOpeningEvent;
import com.ruiyun.jvppeteer.cdp.events.LifecycleEvent;
import com.ruiyun.jvppeteer.cdp.events.LoadingFailedEvent;
import com.ruiyun.jvppeteer.cdp.events.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.cdp.events.MetricsEvent;
import com.ruiyun.jvppeteer.cdp.events.NavigatedWithinDocumentEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestPausedEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestServedFromCacheEvent;
import com.ruiyun.jvppeteer.cdp.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedEvent;
import com.ruiyun.jvppeteer.cdp.events.ResponseReceivedExtraInfoEvent;
import com.ruiyun.jvppeteer.cdp.events.ScreencastFrameEvent;
import com.ruiyun.jvppeteer.cdp.events.ScriptParsedEvent;
import com.ruiyun.jvppeteer.cdp.events.StyleSheetAddedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetCreatedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetDestroyedEvent;
import com.ruiyun.jvppeteer.cdp.events.TargetInfoChangedEvent;
import com.ruiyun.jvppeteer.cdp.events.TracingCompleteEvent;
import com.ruiyun.jvppeteer.transport.CdpCDPSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 存放所用到的常量
 */
public interface Constant {

    /**
     * chrome 临时文件夹前缀
     */
    String CHROME_PROFILE_PREFIX = "jvppeteer_dev_chrome_profile-";
    /**
     * firefox 临时文件夹前缀
     */
    String FIREFOX_PROFILE_PREFIX = "jvppeteer_dev_firefox_profile-";
    /**
     * 把产品存放到环境变量的所有可用字段
     */
    String[] PRODUCT_ENV = {"JVPPETEER_PRODUCT", "java_config_jvppeteer_product", "java_package_config_jvppeteer_product"};

    /**
     * 把浏览器执行路径存放到环境变量的所有可用字段
     */
    String[] EXECUTABLE_ENV = {"JVPPETEER_EXECUTABLE_PATH", "java_config_jvppeteer_executable_path", "java_package_config_jvppeteer_executable_path"};

    String JVPPETEER_TEST_EXPERIMENTAL_CHROME_FEATURES = "JVPPETEER_TEST_EXPERIMENTAL_CHROME_FEATURES";
    /**
     * 把浏览器版本存放到环境变量的字段
     */
    String JVPPETEER_PRODUCT_REVISION_ENV = "JVPPETEER_PRODUCT_REVISION";

    /**
     * 读取流中的数据的buffer size
     */
    int DEFAULT_BUFFER_SIZE = 8 * 1024;
    /**
     * 存放下载浏览器脚本的临时目录
     */
    String SHELLS_PREFIX = "jvppeteer_browser_install_shells-";

    String INSTALL_CHROME_FOR_TESTING_LINUX = "install-chrome-for-testing-linux.sh";
    String INSTALL_CHROME_FOR_TESTING_WIN = "install-chrome-for-testing-win.ps1";
    String INSTALL_CHROME_FOR_TESTING_MAC = "install-chrome-for-testing-mac.sh";
    /**
     * evaluate的js代码添加映射源，方便调试
     */
    Pattern SOURCE_URL_REGEX = Pattern.compile("^[\\040\\t]*//[@#] sourceURL=\\s*(\\S*?)\\s*$", Pattern.MULTILINE);
    /**
     * 启动浏览器时，如果没有指定路径，那么会从以下路径搜索可执行的路径
     */
    String[] PROBABLE_CHROME_EXECUTABLE_PATH =
            new String[]{
                    "/opt/google/chrome/chrome",
                    "/opt/google/chrome-beta/chrome",
                    "/opt/google/chrome-unstable/chrome",
                    "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
                    "/Applications/Google Chrome Beta.app/Contents/MacOS/Google Chrome Beta",
                    "/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary",
                    "/Applications/Google Chrome Dev.app/Contents/MacOS/Google Chrome Dev",
                    "C:/Program Files (x86)/Google/Chrome/Application/chrome.exe",
                    "C:/Program Files/Google/Chrome/Application/chrome.exe",
                    "C:/Program Files (x86)/Google/Chrome Beta/Application/chrome.exe",
                    "C:/Program Files/Google/Chrome Beta/Application/chrome.exe",
                    "C:/Program Files (x86)/Google/Chrome SxS/Application/chrome.exe",
                    "C:/Program Files/Google/Chrome SxS/Application/chrome.exe",
                    "C:/Program Files (x86)/Google/Chrome Dev/Application/chrome.exe",
                    "C:/Program Files/Google/Chrome Dev/Application/chrome.exe"
            };
    /**
     * 谷歌浏览器默认启动参数
     */
    List<String> DEFAULT_ARGS = Collections.unmodifiableList(new ArrayList<String>() {
        private static final long serialVersionUID = 1L;

        {
            addAll(Arrays.asList(
                    "--allow-pre-commit-input",
                    "--disable-background-networking",
                    "--disable-background-timer-throttling",
                    "--disable-backgrounding-occluded-windows",
                    "--disable-breakpad",
                    "--disable-client-side-phishing-detection",
                    "--disable-component-extensions-with-background-pages",
                    "--disable-default-apps",
                    "--disable-dev-shm-usage",
                    "--disable-extensions",
                    "--disable-hang-monitor",
                    "--disable-infobars",
                    "--disable-ipc-flooding-protection",
                    "--disable-popup-blocking",
                    "--disable-prompt-on-repost",
                    "--disable-renderer-backgrounding",
                    "--disable-search-engine-choice-screen",
                    "--disable-sync",
                    "--enable-automation",
                    "--export-tagged-pdf",
                    "--generate-pdf-document-outline",
                    "--force-color-profile=srgb",
                    "--metrics-recording-only",
                    "--no-first-run",
                    "--password-store=basic",
                    "--use-mock-keychain"));
        }
    });


    Set<String> supportedMetrics = new HashSet<String>() {

        private static final long serialVersionUID = -5224857570151968464L;

        {
            add("Timestamp");
            add("Documents");
            add("Frames");
            add("JSEventListeners");
            add("Nodes");
            add("LayoutCount");
            add("RecalcStyleCount");
            add("LayoutDuration");
            add("RecalcStyleDuration");
            add("ScriptDuration");
            add("TaskDuration");
            add("JSHeapUsedSize");
            add("JSHeapTotalSize");
        }
    };
    int NETWORK_IDLE_TIME = 500;
    /**
     * fastjson的一个实例
     */
    ObjectMapper OBJECTMAPPER = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, Visibility.ANY).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * 从浏览器的websocket接受到消息中有以下这些字段，在处理消息用到这些字段
     */
    String METHOD = "method";
    String PARAMS = "params";
    String ID = "id";
    String RESULT = "result";
    String SESSION_ID = "sessionId";
    String SESSION = "session";
    String TARGET_INFO = "targetInfo";
    String TYPE = "type";
    String ERROR = "error";
    String MESSAGE = "message";
    String DATA = "data";
    String TARGET_ID = "targetId";
    String STREAM = "stream";
    String EOF = "eof";
    String BASE_64_ENCODED = "base64Encoded";
    String CODE = "code";


    /**
     * 默认的超时时间：启动浏览器实例超时，websocket接受消息超时等
     */
    int DEFAULT_TIMEOUT = 30_000;

    /**
     * 追踪信息的默认分类
     */
    Set<String> DEFAULTCATEGORIES = new LinkedHashSet<String>() {
        private static final long serialVersionUID = -5224857570151968464L;

        {
            add("-*");
            add("devtools.timeline");
            add("v8.execute");
            add("disabled-by-default-devtools.timeline");
            add("disabled-by-default-devtools.timeline.frame");
            add("toplevel");
            add("blink.console");
            add("blink.user_timing");
            add("latencyInfo");
            add("disabled-by-default-devtools.timeline.stack");
            add("disabled-by-default-v8.cpu_profiler");
//            add("disabled-by-default-v8.cpu_profiler.hires");
        }
    };
    String JVPPETEER_VERSION = "3.4.2";

    String UTILITY_WORLD_NAME = "__puppeteer_utility_world__" + JVPPETEER_VERSION;

    String CDP_BINDING_PREFIX = "puppeteer_";

    int DEFAULT_BATCH_SIZE = 20;

    int TIME_FOR_WAITING_FOR_SWAP = 100;

    String MAIN_WORLD = "mainWorld";

    String PUPPETEER_WORLD = "puppeteerWorld";

    String INTERNAL_URL = "pptr:internal";
    /**
     * 空白页面
     */
    String ABOUT_BLANK = "about:blank";


    String JV_HANDLE_MESSAGE_THREAD = "JvHandleMessageThread-";
    /**
     * connection cdpSession的监听器执行时所对应的类
     */
    Map<String, Class<?>> LISTENER_CLASSES = new HashMap<String, Class<?>>() {
        {
            for (ConnectionEvents event : ConnectionEvents.values()) {
                switch (event.getEventName()) {
                    //cdp
                    case "CDPSession.Disconnected":
                        put(event.getEventName(), null);
                        break;
                    case "sessionattached":
                    case "sessionDetached":
                        put(event.getEventName(), CdpCDPSession.class);
                        break;
                    case "Target.targetCreated":
                        put(event.getEventName(), TargetCreatedEvent.class);
                        break;
                    case "Target.targetDestroyed":
                        put(event.getEventName(), TargetDestroyedEvent.class);
                        break;
                    case "Target.targetInfoChanged":
                        put(event.getEventName(), TargetInfoChangedEvent.class);
                        break;
                    case "Target.attachedToTarget":
                        put(event.getEventName(), AttachedToTargetEvent.class);
                        break;
                    case "Target.detachedFromTarget":
                        put(event.getEventName(), DetachedFromTargetEvent.class);
                        break;
                    case "Page.javascriptDialogOpening":
                        put(event.getEventName(), JavascriptDialogOpeningEvent.class);
                        break;
                    case "Page.screencastFrame":
                        put(event.getEventName(), ScreencastFrameEvent.class);
                        break;
                    case "Runtime.exceptionThrown":
                        put(event.getEventName(), ExceptionThrownEvent.class);
                        break;
                    case "Performance.metrics":
                        put(event.getEventName(), MetricsEvent.class);
                        break;
                    case "Log.entryAdded":
                        put(event.getEventName(), EntryAddedEvent.class);
                        break;
                    case "Page.fileChooserOpened":
                        put(event.getEventName(), FileChooserOpenedEvent.class);
                        break;
                    case "Debugger.scriptParsed":
                        put(event.getEventName(), ScriptParsedEvent.class);
                        break;
                    case "Runtime.executionContextCreated":
                        put(event.getEventName(), ExecutionContextCreatedEvent.class);
                        break;
                    case "Runtime.executionContextDestroyed":
                        put(event.getEventName(), ExecutionContextDestroyedEvent.class);
                        break;
                    case "CSS.styleSheetAdded":
                        put(event.getEventName(), StyleSheetAddedEvent.class);
                        break;
                    case "Page.frameAttached":
                        put(event.getEventName(), FrameAttachedEvent.class);
                        break;
                    case "Page.frameNavigated":
                        put(event.getEventName(), FrameNavigatedEvent.class);
                        break;
                    case "Page.navigatedWithinDocument":
                        put(event.getEventName(), NavigatedWithinDocumentEvent.class);
                        break;
                    case "Page.frameDetached":
                        put(event.getEventName(), FrameDetachedEvent.class);
                        break;
                    case "Page.frameStoppedLoading":
                        put(event.getEventName(), FrameStoppedLoadingEvent.class);
                        break;
                    case "Page.lifecycleEvent":
                        put(event.getEventName(), LifecycleEvent.class);
                        break;
                    case "Fetch.requestPaused":
                        put(event.getEventName(), RequestPausedEvent.class);
                        break;
                    case "Fetch.authRequired":
                        put(event.getEventName(), AuthRequiredEvent.class);
                        break;
                    case "Network.requestWillBeSent":
                        put(event.getEventName(), RequestWillBeSentEvent.class);
                        break;
                    case "Network.requestServedFromCache":
                        put(event.getEventName(), RequestServedFromCacheEvent.class);
                        break;
                    case "Network.responseReceived":
                        put(event.getEventName(), ResponseReceivedEvent.class);
                        break;
                    case "Network.loadingFinished":
                        put(event.getEventName(), LoadingFinishedEvent.class);
                        break;
                    case "Network.loadingFailed":
                        put(event.getEventName(), LoadingFailedEvent.class);
                        break;
                    case "Runtime.consoleAPICalled":
                        put(event.getEventName(), ConsoleAPICalledEvent.class);
                        break;
                    case "Runtime.bindingCalled":
                        put(event.getEventName(), BindingCalledEvent.class);
                        break;
                    case "Tracing.tracingComplete":
                        put(event.getEventName(), TracingCompleteEvent.class);
                        break;
                    case "Page.frameStartedLoading":
                        put(event.getEventName(), FrameStartedLoadingEvent.class);
                        break;
                    case "Network.responseReceivedExtraInfo":
                        put(event.getEventName(), ResponseReceivedExtraInfoEvent.class);
                        break;
                    case "Input.dragIntercepted":
                        put(event.getEventName(), DragInterceptedEvent.class);
                        break;
                    case "Browser.downloadProgress":
                        put(event.getEventName(), DownloadProgressEvent.class);
                        break;
                    case "Browser.downloadWillBegin":
                        put(event.getEventName(), DownloadWillBeginEvent.class);
                        break;
                    //bidi
                    case "browsingContext.contextCreated":
                    case "browsingContext.contextDestroyed":
                        put(event.getEventName(), ContextCreatedEvent.class);
                        break;
                    case "ended":
                        put(event.getEventName(), ClosedEvent.class);
                        break;
                    case "browsingContext.navigationAborted":
                    case "browsingContext.navigationFailed":
                    case "browsingContext.fragmentNavigated":
                    case "browsingContext.navigationStarted":
                        put(event.getEventName(), NavigationInfoEvent.class);
                        break;
                    case "script.realmCreated":
                        put(event.getEventName(), RealmInfo.class);
                        break;
                    case "script.realmDestroyed":
                        put(event.getEventName(), RealmDestroyedParameters.class);
                        break;
                    case "browsingContext.load":
                    case "browsingContext.domContentLoaded":
                        put(event.getEventName(), NavigationInfoEvent.class);
                        break;
                    case "network.authRequired":
                        put(event.getEventName(), AuthRequiredParameters.class);
                        break;
                    case "network.fetchError":
                        put(event.getEventName(), FetchErrorParameters.class);
                        break;
                    case "network.responseCompleted":
                        put(event.getEventName(), ResponseCompletedParameters.class);
                        break;
                    case "network.beforeRequestSent":
                        put(event.getEventName(), BaseParameters.class);
                        break;
                    case "network.responseStarted":
                        put(event.getEventName(), ResponseStartedParameters.class);
                        break;
                    case "log.entryAdded":
                        put(event.getEventName(), LogEntry.class);
                        break;
                    case "browsingContext.userPromptOpened":
                        put(event.getEventName(), UserPromptOpenedParameters.class);
                        break;
                    case "browsingContext.userPromptClosed":
                        put(event.getEventName(), UserPromptClosedParameters.class);
                        break;
                    case "script.message":
                        put(event.getEventName(), MessageParameters.class);
                        break;
                    case "input.fileDialogOpened":
                        put(event.getEventName(), FileDialogInfo.class);
                        break;
                    case "DeviceAccess.deviceRequestPrompted":
                        put(event.getEventName(), DeviceRequestPromptedEvent.class);
                        break;
                    case "Network.requestWillBeSentExtraInfo":
                        put(event.getEventName(), RequestWillBeSentExtraInfoEvent.class);
                        break;
                }
            }
        }
    };


    Map<Integer, String> CLOSE_REASON = new HashMap<Integer, String>() {{
        put(1000, "Normal closure");
        put(1001, "Endpoint is leaving, possibly due to a server error or because the browser is navigating away from the page with the open connection");
        put(1002, "Connection closed due to a protocol error");
        put(1003, "Connection closed due to receiving an unsupported data type (e.g., a text-only endpoint received binary data)");
        put(1005, "An expected status code was not received");
        put(1006, "Connection is abnormally closed without sending a close frame when expecting a status code");
        put(1007, "Connection closed due to receiving data in an invalid format (e.g., non-UTF-8 data in a text message)");
        put(1008, "Connection closed due to receiving data that violates the policy. This is a generic status code used when 1003 and 1009 are not applicable");
        put(1009, "Connection closed due to receiving a too large data frame");
        put(1010, "Client expects the server to negotiate one or more extensions, but the server did not handle them, so the client closes the connection");
        put(1011, "Server closes the connection because the client encountered an unexpected situation preventing it from completing the request");
        put(1012, "Server closes the connection due to a restart. This is a generic status code used when 1001 is not applicable");
        put(1013, "Server closes the connection for temporary reasons, such as server overload leading to closing some client connections");
        put(1015, "Connection was closed because the TLS handshake could not be completed");
        put(-1, "Connection has not yet been opened");
        put(-2, "Connection was closed due to an internal error");
        put(-3, "Connection was closed due to failing to complete the Flash policy check");
    }};

    Map<WebPermission, String> WEB_PERMISSION_TO_PROTOCOL_PERMISSION = new HashMap<WebPermission, String>(32) {
        {
            put(WebPermission.Accelerometer, "sensors");
            put(WebPermission.Ambient_light_sensor, "sensors");
            put(WebPermission.Background_sync, "backgroundSync");
            put(WebPermission.Camera, "videoCapture");
            put(WebPermission.Clipboard_read, "clipboardReadWrite");
            put(WebPermission.Clipboard_sanitized_write, "clipboardSanitizedWrite");
            put(WebPermission.Clipboard_write, "clipboardReadWrite");
            put(WebPermission.Geolocation, "geolocation");
            put(WebPermission.Gyroscope, "sensors");
            put(WebPermission.Idle_detection, "idleDetection");
            put(WebPermission.Keyboard_lock, "keyboardLock");
            put(WebPermission.Magnetometer, "sensors");
            put(WebPermission.Microphone, "audioCapture");
            put(WebPermission.Midi, "midi");
            put(WebPermission.Notifications, "notifications");
            put(WebPermission.Payment_handler, "paymentHandler");
            put(WebPermission.Persistent_storage, "durableStorage");
            put(WebPermission.Pointer_lock, "pointerLock");
            // chrome-specific permissions we have.
            put(WebPermission.Midi_sysex, "midiSysex");
        }
    };

    List<String> EVENTS = Arrays.stream(ConnectionEvents.values()).map(ConnectionEvents::getEventName).collect(Collectors.toList());

    String CDP_SPECIFIC_PREFIX = "goog:";
    String PREFS_JS = "prefs.js";
    String USER_JS = "user.js";
    String BACKUP_SUFFIX = ".bak";
    /**
     * 应用与 PuppeteerUtil
     */
    String Source = "(() => {\n      const module = {};\n      \"use strict\";var g=Object.defineProperty;var X=Object.getOwnPropertyDescriptor;var B=Object.getOwnPropertyNames;var Y=Object.prototype.hasOwnProperty;var l=(t,e)=>{for(var r in e)g(t,r,{get:e[r],enumerable:!0})},J=(t,e,r,o)=>{if(e&&typeof e==\"object\"||typeof e==\"function\")for(let n of B(e))!Y.call(t,n)&&n!==r&&g(t,n,{get:()=>e[n],enumerable:!(o=X(e,n))||o.enumerable});return t};var z=t=>J(g({},\"__esModule\",{value:!0}),t);var pe={};l(pe,{default:()=>he});module.exports=z(pe);var N=class extends Error{constructor(e,r){super(e,r),this.name=this.constructor.name}get[Symbol.toStringTag](){return this.constructor.name}},p=class extends N{};var c=class t{static create(e){return new t(e)}static async race(e){let r=new Set;try{let o=e.map(n=>n instanceof t?(n.#n&&r.add(n),n.valueOrThrow()):n);return await Promise.race(o)}finally{for(let o of r)o.reject(new Error(\"Timeout cleared\"))}}#e=!1;#r=!1;#o;#t;#a=new Promise(e=>{this.#t=e});#n;#i;constructor(e){e&&e.timeout>0&&(this.#i=new p(e.message),this.#n=setTimeout(()=>{this.reject(this.#i)},e.timeout))}#l(e){clearTimeout(this.#n),this.#o=e,this.#t()}resolve(e){this.#r||this.#e||(this.#e=!0,this.#l(e))}reject(e){this.#r||this.#e||(this.#r=!0,this.#l(e))}resolved(){return this.#e}finished(){return this.#e||this.#r}value(){return this.#o}#s;valueOrThrow(){return this.#s||(this.#s=(async()=>{if(await this.#a,this.#r)throw this.#o;return this.#o})()),this.#s}};var L=new Map,F=t=>{let e=L.get(t);return e||(e=new Function(`return ${t}`)(),L.set(t,e),e)};var x={};l(x,{ariaQuerySelector:()=>G,ariaQuerySelectorAll:()=>b});var G=(t,e)=>globalThis.__ariaQuerySelector(t,e),b=async function*(t,e){yield*await globalThis.__ariaQuerySelectorAll(t,e)};var E={};l(E,{cssQuerySelector:()=>K,cssQuerySelectorAll:()=>Z});var K=(t,e)=>t.querySelector(e),Z=function(t,e){return t.querySelectorAll(e)};var A={};l(A,{customQuerySelectors:()=>P});var v=class{#e=new Map;register(e,r){if(!r.queryOne&&r.queryAll){let o=r.queryAll;r.queryOne=(n,i)=>{for(let s of o(n,i))return s;return null}}else if(r.queryOne&&!r.queryAll){let o=r.queryOne;r.queryAll=(n,i)=>{let s=o(n,i);return s?[s]:[]}}else if(!r.queryOne||!r.queryAll)throw new Error(\"At least one query method must be defined.\");this.#e.set(e,{querySelector:r.queryOne,querySelectorAll:r.queryAll})}unregister(e){this.#e.delete(e)}get(e){return this.#e.get(e)}clear(){this.#e.clear()}},P=new v;var R={};l(R,{pierceQuerySelector:()=>ee,pierceQuerySelectorAll:()=>te});var ee=(t,e)=>{let r=null,o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&!r&&s.matches(e)&&(r=s)}while(!r&&i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r},te=(t,e)=>{let r=[],o=n=>{let i=document.createTreeWalker(n,NodeFilter.SHOW_ELEMENT);do{let s=i.currentNode;s.shadowRoot&&o(s.shadowRoot),!(s instanceof ShadowRoot)&&s!==n&&s.matches(e)&&r.push(s)}while(i.nextNode())};return t instanceof Document&&(t=t.documentElement),o(t),r};var u=(t,e)=>{if(!t)throw new Error(e)};var y=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=new MutationObserver(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())}),this.#o.observe(this.#r,{childList:!0,subtree:!0,attributes:!0})}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(this.#o.disconnect(),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}},w=class{#e;#r;constructor(e){this.#e=e}async start(){let e=this.#r=c.create(),r=await this.#e();if(r){e.resolve(r);return}let o=async()=>{if(e.finished())return;let n=await this.#e();if(!n){window.requestAnimationFrame(o);return}e.resolve(n),await this.stop()};window.requestAnimationFrame(o)}async stop(){u(this.#r,\"Polling never started.\"),this.#r.finished()||this.#r.reject(new Error(\"Polling stopped\"))}result(){return u(this.#r,\"Polling never started.\"),this.#r.valueOrThrow()}},S=class{#e;#r;#o;#t;constructor(e,r){this.#e=e,this.#r=r}async start(){let e=this.#t=c.create(),r=await this.#e();if(r){e.resolve(r);return}this.#o=setInterval(async()=>{let o=await this.#e();o&&(e.resolve(o),await this.stop())},this.#r)}async stop(){u(this.#t,\"Polling never started.\"),this.#t.finished()||this.#t.reject(new Error(\"Polling stopped\")),this.#o&&(clearInterval(this.#o),this.#o=void 0)}result(){return u(this.#t,\"Polling never started.\"),this.#t.valueOrThrow()}};var _={};l(_,{PCombinator:()=>H,pQuerySelector:()=>fe,pQuerySelectorAll:()=>$});var a=class{static async*map(e,r){for await(let o of e)yield await r(o)}static async*flatMap(e,r){for await(let o of e)yield*r(o)}static async collect(e){let r=[];for await(let o of e)r.push(o);return r}static async first(e){for await(let r of e)return r}};var C={};l(C,{textQuerySelectorAll:()=>m});var re=new Set([\"checkbox\",\"image\",\"radio\"]),oe=t=>t instanceof HTMLSelectElement||t instanceof HTMLTextAreaElement||t instanceof HTMLInputElement&&!re.has(t.type),ne=new Set([\"SCRIPT\",\"STYLE\"]),f=t=>!ne.has(t.nodeName)&&!document.head?.contains(t),I=new WeakMap,j=t=>{for(;t;)I.delete(t),t instanceof ShadowRoot?t=t.host:t=t.parentNode},W=new WeakSet,se=new MutationObserver(t=>{for(let e of t)j(e.target)}),d=t=>{let e=I.get(t);if(e||(e={full:\"\",immediate:[]},!f(t)))return e;let r=\"\";if(oe(t))e.full=t.value,e.immediate.push(t.value),t.addEventListener(\"input\",o=>{j(o.target)},{once:!0,capture:!0});else{for(let o=t.firstChild;o;o=o.nextSibling){if(o.nodeType===Node.TEXT_NODE){e.full+=o.nodeValue??\"\",r+=o.nodeValue??\"\";continue}r&&e.immediate.push(r),r=\"\",o.nodeType===Node.ELEMENT_NODE&&(e.full+=d(o).full)}r&&e.immediate.push(r),t instanceof Element&&t.shadowRoot&&(e.full+=d(t.shadowRoot).full),W.has(t)||(se.observe(t,{childList:!0,characterData:!0,subtree:!0}),W.add(t))}return I.set(t,e),e};var m=function*(t,e){let r=!1;for(let o of t.childNodes)if(o instanceof Element&&f(o)){let n;o.shadowRoot?n=m(o.shadowRoot,e):n=m(o,e);for(let i of n)yield i,r=!0}r||t instanceof Element&&f(t)&&d(t).full.includes(e)&&(yield t)};var k={};l(k,{checkVisibility:()=>le,pierce:()=>T,pierceAll:()=>O});var ie=[\"hidden\",\"collapse\"],le=(t,e)=>{if(!t)return e===!1;if(e===void 0)return t;let r=t.nodeType===Node.TEXT_NODE?t.parentElement:t,o=window.getComputedStyle(r),n=o&&!ie.includes(o.visibility)&&!ae(r);return e===n?t:!1};function ae(t){let e=t.getBoundingClientRect();return e.width===0||e.height===0}var ce=t=>\"shadowRoot\"in t&&t.shadowRoot instanceof ShadowRoot;function*T(t){ce(t)?yield t.shadowRoot:yield t}function*O(t){t=T(t).next().value,yield t;let e=[document.createTreeWalker(t,NodeFilter.SHOW_ELEMENT)];for(let r of e){let o;for(;o=r.nextNode();)o.shadowRoot&&(yield o.shadowRoot,e.push(document.createTreeWalker(o.shadowRoot,NodeFilter.SHOW_ELEMENT)))}}var Q={};l(Q,{xpathQuerySelectorAll:()=>q});var q=function*(t,e,r=-1){let n=(t.ownerDocument||document).evaluate(e,t,null,XPathResult.ORDERED_NODE_ITERATOR_TYPE),i=[],s;for(;(s=n.iterateNext())&&(i.push(s),!(r&&i.length===r)););for(let h=0;h<i.length;h++)s=i[h],yield s,delete i[h]};var ue=/[-\\w\\P{ASCII}*]/u,H=(r=>(r.Descendent=\">>>\",r.Child=\">>>>\",r))(H||{}),V=t=>\"querySelectorAll\"in t,M=class{#e;#r=[];#o=void 0;elements;constructor(e,r){this.elements=[e],this.#e=r,this.#t()}async run(){if(typeof this.#o==\"string\")switch(this.#o.trimStart()){case\":scope\":this.#t();break}for(;this.#o!==void 0;this.#t()){let e=this.#o;typeof e==\"string\"?e[0]&&ue.test(e[0])?this.elements=a.flatMap(this.elements,async function*(r){V(r)&&(yield*r.querySelectorAll(e))}):this.elements=a.flatMap(this.elements,async function*(r){if(!r.parentElement){if(!V(r))return;yield*r.querySelectorAll(e);return}let o=0;for(let n of r.parentElement.children)if(++o,n===r)break;yield*r.parentElement.querySelectorAll(`:scope>:nth-child(${o})${e}`)}):this.elements=a.flatMap(this.elements,async function*(r){switch(e.name){case\"text\":yield*m(r,e.value);break;case\"xpath\":yield*q(r,e.value);break;case\"aria\":yield*b(r,e.value);break;default:let o=P.get(e.name);if(!o)throw new Error(`Unknown selector type: ${e.name}`);yield*o.querySelectorAll(r,e.value)}})}}#t(){if(this.#r.length!==0){this.#o=this.#r.shift();return}if(this.#e.length===0){this.#o=void 0;return}let e=this.#e.shift();switch(e){case\">>>>\":{this.elements=a.flatMap(this.elements,T),this.#t();break}case\">>>\":{this.elements=a.flatMap(this.elements,O),this.#t();break}default:this.#r=e,this.#t();break}}},D=class{#e=new WeakMap;calculate(e,r=[]){if(e===null)return r;e instanceof ShadowRoot&&(e=e.host);let o=this.#e.get(e);if(o)return[...o,...r];let n=0;for(let s=e.previousSibling;s;s=s.previousSibling)++n;let i=this.calculate(e.parentNode,[n]);return this.#e.set(e,i),[...i,...r]}},U=(t,e)=>{if(t.length+e.length===0)return 0;let[r=-1,...o]=t,[n=-1,...i]=e;return r===n?U(o,i):r<n?-1:1},de=async function*(t){let e=new Set;for await(let o of t)e.add(o);let r=new D;yield*[...e.values()].map(o=>[o,r.calculate(o)]).sort(([,o],[,n])=>U(o,n)).map(([o])=>o)},$=function(t,e){let r=JSON.parse(e);if(r.some(o=>{let n=0;return o.some(i=>(typeof i==\"string\"?++n:n=0,n>1))}))throw new Error(\"Multiple deep combinators found in sequence.\");return de(a.flatMap(r,o=>{let n=new M(t,o);return n.run(),n.elements}))},fe=async function(t,e){for await(let r of $(t,e))return r;return null};var me=Object.freeze({...x,...A,...R,..._,...C,...k,...Q,...E,Deferred:c,createFunction:F,createTextContent:d,IntervalPoller:S,isSuitableNodeForTextMatching:f,MutationPoller:y,RAFPoller:w}),he=me;\n\n      \n      return module.exports.default;\n    })()";
    String NaN = "NaN";
    String Infinity = "Infinity";
    String Navigate_Infinity = "-Infinity";
    String Navigate_Zero = "-0";
}
