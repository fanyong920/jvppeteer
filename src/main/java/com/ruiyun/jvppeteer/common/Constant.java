package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruiyun.jvppeteer.entities.DragInterceptedEvent;
import com.ruiyun.jvppeteer.events.AttachedToTargetEvent;
import com.ruiyun.jvppeteer.events.AuthRequiredEvent;
import com.ruiyun.jvppeteer.events.BindingCalledEvent;
import com.ruiyun.jvppeteer.events.ConsoleAPICalledEvent;
import com.ruiyun.jvppeteer.events.DetachedFromTargetEvent;
import com.ruiyun.jvppeteer.events.DownloadProgressEvent;
import com.ruiyun.jvppeteer.events.DownloadWillBeginEvent;
import com.ruiyun.jvppeteer.events.EntryAddedEvent;
import com.ruiyun.jvppeteer.events.ExceptionThrownEvent;
import com.ruiyun.jvppeteer.events.ExecutionContextCreatedEvent;
import com.ruiyun.jvppeteer.events.ExecutionContextDestroyedEvent;
import com.ruiyun.jvppeteer.events.FileChooserOpenedEvent;
import com.ruiyun.jvppeteer.events.FrameAttachedEvent;
import com.ruiyun.jvppeteer.events.FrameDetachedEvent;
import com.ruiyun.jvppeteer.events.FrameNavigatedEvent;
import com.ruiyun.jvppeteer.events.FrameStartedLoadingEvent;
import com.ruiyun.jvppeteer.events.FrameStoppedLoadingEvent;
import com.ruiyun.jvppeteer.events.JavascriptDialogOpeningEvent;
import com.ruiyun.jvppeteer.events.LifecycleEvent;
import com.ruiyun.jvppeteer.events.LoadingFailedEvent;
import com.ruiyun.jvppeteer.events.LoadingFinishedEvent;
import com.ruiyun.jvppeteer.events.MetricsEvent;
import com.ruiyun.jvppeteer.events.NavigatedWithinDocumentEvent;
import com.ruiyun.jvppeteer.events.RequestPausedEvent;
import com.ruiyun.jvppeteer.events.RequestServedFromCacheEvent;
import com.ruiyun.jvppeteer.events.RequestWillBeSentEvent;
import com.ruiyun.jvppeteer.events.ResponseReceivedEvent;
import com.ruiyun.jvppeteer.events.ResponseReceivedExtraInfoEvent;
import com.ruiyun.jvppeteer.events.ScreencastFrameEvent;
import com.ruiyun.jvppeteer.events.ScriptParsedEvent;
import com.ruiyun.jvppeteer.events.StyleSheetAddedEvent;
import com.ruiyun.jvppeteer.events.TargetCreatedEvent;
import com.ruiyun.jvppeteer.events.TargetDestroyedEvent;
import com.ruiyun.jvppeteer.events.TargetInfoChangedEvent;
import com.ruiyun.jvppeteer.events.TracingCompleteEvent;
import com.ruiyun.jvppeteer.transport.CDPSession;

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

/**
 * 存放所用到的常量
 */
public interface Constant {

    /**
     * 默认浏览器版本，最好使用默认指定的版本，否则有些cdp api参数会失效
     */
    String VERSION = "128.0.6613.137";
    /**
     * 临时文件夹前缀
     */
    String PROFILE_PREFIX = "jvppeteer_dev_chrome_profile-";
    /**
     * 把产品存放到环境变量的所有可用字段
     */
    String[] PRODUCT_ENV = {"JVPPETEER_PRODUCT", "java_config_jvppeteer_product", "java_package_config_jvppeteer_product"};

    /**
     * 把浏览器执行路径存放到环境变量的所有可用字段
     */
    String[] EXECUTABLE_ENV = {"JVPPETEER_EXECUTABLE_PATH", "java_config_jvppeteer_executable_path", "java_package_config_jvppeteer_executable_path"};

    /**
     * 把浏览器版本存放到环境变量的字段
     */
    String JVPPETEER_CHROMIUM_REVISION_ENV = "JVPPETEER_CHROMIUM_REVISION";

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
                    "--disable-component-update",
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
    int DEFAULT_TIMEOUT = 30000;

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
    /**
     * 从websocket接受到消息后，如果这个消息包含有事件并且该事件有监听器，则放到这个线程中执行
     */
    String JV_EMIT_EVENT_THREAD = "JvEmitEventThread-";

    String JV_HANDLE_MESSAGE_THREAD = "JvHandleMessageThread-";
    /**
     * connection cdpsession的监听器执行时所对应的类
     */
    Map<String, Class<?>>  LISTENER_CLASSES = new HashMap<String, Class<?>>() {
        {
            for (CDPSession.CDPSessionEvent event : CDPSession.CDPSessionEvent.values()) {
                switch (event.getEventName()) {
                    case "CDPSession.Disconnected":
                        put(event.getEventName(), null);
                        break;
                    case "sessionattached":
                    case "sessionDetached":
                        put(event.getEventName(), CDPSession.class);
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
            put(WebPermission.GEOLOCATION, "geolocation");
            put(WebPermission.MIDI, "midi");
            put(WebPermission.NOTIFICATIONS, "notifications");
            put(WebPermission.CAMERA, "videoCapture");
            put(WebPermission.MICROPHONE, "audioCapture");
            put(WebPermission.BACKGROUND_SYNC, "backgroundSync");
            put(WebPermission.AMBIENT_LIGHT_SENSOR, "sensors");
            put(WebPermission.ACCELEROMETER, "sensors");
            put(WebPermission.GYROSCOPE, "sensors");
            put(WebPermission.MAGNETOMETER, "sensors");
            put(WebPermission.ACCESSIBILITY_EVENTS, "accessibilityEvents");
            put(WebPermission.CLIPBOARD_READ, "clipboardReadWrite");
            put(WebPermission.CLIPBOARD_WRITE, "clipboardReadWrite");
            put(WebPermission.CLIPBOARD_SANITIZED_WRITE, "clipboardSanitizedWrite");
            put(WebPermission.PAYMENT_HANDLER, "paymentHandler");
            put(WebPermission.PERSISTENT_STORAGE, "durableStorage");
            put(WebPermission.IDLE_DETECTION, "idleDetection");
            put(WebPermission.MIDI_SYSEX, "midiSysex");
        }
    };
}
