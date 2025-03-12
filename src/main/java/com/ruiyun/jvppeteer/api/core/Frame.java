package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.events.FrameEvents;
import com.ruiyun.jvppeteer.cdp.core.Accessibility;
import com.ruiyun.jvppeteer.cdp.entities.ClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddScriptTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.FrameAddStyleTagOptions;
import com.ruiyun.jvppeteer.cdp.entities.GoToOptions;
import com.ruiyun.jvppeteer.cdp.entities.WaitForOptions;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.common.DeviceRequestPrompt;
import com.ruiyun.jvppeteer.common.QuerySelector;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.GetQueryHandler;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


import static com.ruiyun.jvppeteer.common.Constant.DEFAULT_BATCH_SIZE;
import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public abstract class Frame extends EventEmitter<FrameEvents> {
    protected volatile String id;
    protected volatile String parentId;
    protected Accessibility accessibility;
    public volatile boolean hasStartedLoading;
    protected volatile String name;
    protected ElementHandle document;

    public Frame() {
        super();
    }

    /**
     * 与框架关联的页面。
     *
     * @return 与框架关联的页面。
     */
    public abstract Page page();

    /**
     * 将框架或页面导航到给定的 url。
     *
     * @param url           将框架导航到的 URL。URL 应包含方案，例如 https://
     * @param options       可选的配置等待行为的选项。
     * @return waitForResult = true 返回页面导航的响应，如果存在多个重定向，导航将使用最后一个重定向的响应进行解析。否则返回 null
     */
    public abstract Response goTo(String url, GoToOptions options);

    /**
     * 等到导航完成
     *
     * @param options        可选的等待导航选项
     * @param navigateRunner 一个需要的执行的步骤
     * @return 导航的响应
     */
    public abstract Response waitForNavigation(WaitForOptions options, Runnable navigateRunner);

    /**
     * 当前框架的客户端
     *
     * @return 客户端
     */
    public abstract CDPSession client();

    public abstract Accessibility accessibility();

    public abstract Realm mainRealm();

    public abstract Realm isolatedRealm();

    /**
     * 返回当前框架的 Document 对象
     *
     * @return 当前框架的 Document 对象
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       执行 JS 函数异常
     */
    public ElementHandle document() throws JsonProcessingException {
        if (this.document == null) {
            this.document = this.mainRealm().evaluateHandle("() => {\n" +
                    "        return document;\n" +
                    "      }", null).asElement();
        }
        return this.document;
    }

    /**
     * 用来清理已经被释放的 Document 句柄
     */
    public void clearDocumentHandle() {
        this.document = null;
    }

    /**
     * 返回与此框架相关联的元素
     *
     * @return 与此框架相关联的元素
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public ElementHandle frameElement() throws JsonProcessingException {
        Frame parentFrame = this.parentFrame();
        if (Objects.isNull(parentFrame)) {
            return null;
        }
        JSHandle list = parentFrame.isolatedRealm().evaluateHandle("() => {\n" +
                "      return document.querySelectorAll('iframe,frame');\n" +
                "    }", null);
        ElementHandle result = null;
        List<JSHandle> lists = transposeIterableHandle(list);
        try {
            Iterator<JSHandle> iterator = lists.iterator();
            while (iterator.hasNext()) {
                JSHandle iframe = iterator.next();
                Frame frame = iframe.asElement().contentFrame();
                if (frame != null && frame.id().equals(this.id)) {
                    result = iframe.asElement();
                    iterator.remove();
                    break;
                }
            }
        } finally {
            lists.forEach(JSHandle::dispose);
            Optional.of(list).ifPresent(JSHandle::dispose);
        }
        if (Objects.isNull(result)) {
            return null;
        }
        return parentFrame.mainRealm().adoptHandle(result);
    }

    /**
     * 行为与 Page.evaluateHandle() 相同，只是它在此框架的上下文中运行。
     * <p>
     * 详情请参阅 Page.evaluateHandle()。
     *
     * @param pptrFunction 给定的 JS 函数
     * @param args         JS 函数的参数
     * @return pptrFunction 执行的结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        return this.mainRealm().evaluateHandle(pptrFunction, args);
    }

    /**
     * 行为与 Page.evaluate() 相同，只是它在此框架的上下文中运行。
     * <p>
     * 详情请参阅 Page.evaluate()。
     *
     * @param pptrFunction 给定的 JS 函数
     * @param type         指定了 pptrFunction 的类型
     * @param args         pptrFunction 的参数
     * @return pptrFunction 的执行结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public Object evaluate(String pptrFunction, EvaluateType type, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluate", pptrFunction);
        return this.mainRealm().evaluate(pptrFunction, type, args);
    }

    /**
     * 行为与 Page.evaluate() 相同，只是它在此框架的上下文中运行。
     * <p>
     * 详情请参阅 Page.evaluate()。
     *
     * @param pptrFunction 给定的 JS 函数
     * @return pptrFunction 的执行结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public Object evaluate(String pptrFunction) throws JsonProcessingException {
        return this.evaluate(pptrFunction, null, null);
    }

    /**
     * 行为与 Page.evaluate() 相同，只是它在此框架的上下文中运行。
     * <p>
     * 详情请参阅 Page.evaluate()。
     *
     * @param pptrFunction 给定的 JS 函数
     * @param args         pptrFunction 的参数
     * @return pptrFunction 的执行结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public Object evaluate(String pptrFunction, List<Object> args) throws JsonProcessingException {
        return this.evaluate(pptrFunction, null, args);
    }

    /**
     * 查询框架中与给定选择器匹配的第一个元素。
     *
     * @param selector 选择器
     * @return 与给定选择器匹配的第一个元素
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public ElementHandle $(String selector) throws JsonProcessingException {
        ElementHandle document = this.document();
        return document.$(selector);
    }

    /**
     * 查询框架中与给定选择器匹配的所有元素。
     *
     * @param selector 选择器
     * @return 与给定选择器匹配的所有元素
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public List<ElementHandle> $$(String selector) throws JsonProcessingException {
        ElementHandle document = this.document();
        return document.$$(selector);
    }

    /**
     * 对与框架中给定选择器匹配的第一个元素运行给定 JS 函数。
     * <p>
     * 如果给定的函数返回一个 Promise，那么此方法将等待直到 Promise 解析。
     *
     * @param selector     选择器
     * @param pptrFunction 给定 JS 函数
     * @param args         pptrFunction 的参数
     * @return pptrFunction 运行的结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public Object $eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("$eval", pptrFunction);
        ElementHandle document = this.document();
        return document.$eval(selector, pptrFunction, args);
    }

    /**
     * 对与框架中给定选择器匹配的元素数组运行给定 JS 函数。
     * <p>
     * 如果给定的函数返回一个 Promise，那么此方法将等待直到 Promise 解析。
     *
     * @param selector     选择器
     * @param pptrFunction 给定 JS 函数
     * @param args         pptrFunction 的参数
     * @return pptrFunction 运行的结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public Object $$eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("$$eval", pptrFunction);
        ElementHandle document = this.document();
        return document.$$eval(selector, pptrFunction, args);
    }

    /**
     * 等待直到指定的选择器匹配的元素满足某些条件（可见、隐藏或存在）.
     *
     * @param selector 选择器字符串，用于选择目标元素.
     * @param options  包含等待条件的选项，如元素可见或隐藏.
     * @return 返回匹配选择器的目标元素的句柄.
     */
    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws JsonProcessingException {
        QuerySelector querySelector = GetQueryHandler.getQueryHandlerAndSelector(selector,this);
        options.setPolling(querySelector.getPolling());
        return querySelector.getQueryHandler().waitFor(this, querySelector.getUpdatedSelector(), options);
    }

    /**
     * 等待 pptrFunction 执行完成
     *
     * @param pptrFunction 等待的函数
     * @param options      可选配置
     * @param args         pptrFunction 的参数
     * @return pptrFunction 执行的结果
     */
    public JSHandle waitForFunction(String pptrFunction, WaitForSelectorOptions options,EvaluateType type, Object... args) throws ExecutionException, InterruptedException, TimeoutException {
        return this.mainRealm().waitForFunction(pptrFunction, options, type, args);
    }

    /**
     * 框架的完整 HTML 内容，包括 DOCTYPE。
     *
     * @return 框架的完整 HTML 内容，包括 DOCTYPE。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public String content() throws JsonProcessingException, EvaluateException {
        return (String) this.evaluate("() => {\n" +
                "      let content = '';\n" +
                "      for (const node of document.childNodes) {\n" +
                "        switch (node) {\n" +
                "          case document.documentElement:\n" +
                "            content += document.documentElement.outerHTML;\n" +
                "            break;\n" +
                "          default:\n" +
                "            content += new XMLSerializer().serializeToString(node);\n" +
                "            break;\n" +
                "        }\n" +
                "      }\n" +
                "\n" +
                "      return content;\n" +
                "    }");
    }

    /**
     * 设置框架的内容
     *
     * @param html    要分配给页面的 HTML 标记。
     * @param options （可选的）用于配置超时前多长时间以及何时认为内容设置成功的选项。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public abstract void setContent(String html, WaitForOptions options) throws JsonProcessingException, InterruptedException, ExecutionException;

    /**
     * 设置框架的内容
     *
     * @param content 要分配给页面的 HTML 标记。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void setFrameContent(String content) throws JsonProcessingException {
        this.evaluate("(content) => {\n" +
                "      document.open();\n" +
                "      document.write(content);\n" +
                "      document.close();\n" +
                "    }", Collections.singletonList(content));
    }

    /**
     * 框架的名字
     *
     * @return 框架的名字
     */
    public String name() {
        return this.name == null ? "" : this.name;
    }

    /**
     * 框架的 url
     *
     * @return 框架的 url
     */
    public abstract String url();

    /**
     * 父框架（如果有）。分离框架和主框架返回 null。
     *
     * @return 父框架
     */
    public abstract Frame parentFrame();

    /**
     * 子框架数组。
     *
     * @return 子框架数组。
     */
    public abstract <FrameType extends Frame> List<FrameType> childFrames();

    /**
     * Is`true` if the frame has been detached. Otherwise, `false`.
     *
     * @return true or false
     */
    public abstract boolean detached();

    public boolean disposed() {
        return this.detached();
    }


    /**
     * 在当前文档中添加一个脚本标签。
     * <p>
     * 此方法使用提供的选项创建一个脚本标签，可以是通过URL、文件路径或直接通过内容来加载脚本。
     * 它支持异步执行，并处理脚本加载的生命周期事件，如'load'和'error'。
     *
     * @param options 脚本标签的选项，包括URL、路径或内容等。
     * @return 返回新创建的脚本元素的句柄。
     * @throws IOException       当读取脚本文件时发生IO错误。
     * @throws EvaluateException 当脚本执行失败时抛出。
     */
    public ElementHandle addScriptTag(FrameAddScriptTagOptions options) throws IOException, EvaluateException {
        if (options == null) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getUrl()) && StringUtil.isEmpty(options.getPath()) && StringUtil.isEmpty(options.getContent())) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getType())) {
            options.setType("text/javascript");
        }
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            options.setContent(String.join("\n", contents) + "//# sourceURL=" + options.getPath().replaceAll("\n", ""));
        }
        return this.mainRealm().evaluateHandle("async ({url, id, type, content}) => {\n" +
                "    return await new Promise((resolve, reject) => {\n" +
                "      const script = document.createElement('script');\n" +
                "      script.type = type;\n" +
                "      script.text = content;\n" +
                "      script.addEventListener(\n" +
                "        'error',\n" +
                "        event => {\n" +
                "          reject(new Error(event.message ?? 'Could not load script'));\n" +
                "        },\n" +
                "        {once: true}\n" +
                "      );\n" +
                "      if (id) {\n" +
                "        script.id = id;\n" +
                "      }\n" +
                "      if (url) {\n" +
                "        script.src = url;\n" +
                "        script.addEventListener(\n" +
                "          'load',\n" +
                "          () => {\n" +
                "            resolve(script);\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        document.head.appendChild(script);\n" +
                "      } else {\n" +
                "        document.head.appendChild(script);\n" +
                "        resolve(script);\n" +
                "      }\n" +
                "    });\n" +
                "  }", Collections.singletonList(options)).asElement();

    }

    /**
     * 向文档头部添加样式标签
     * <p>
     * 该方法用于在HTML文档的头部内插入一个新的样式标签。它支持通过URL链接到外部样式表，
     * 或者直接包含样式内容。当提供样式文件的路径时，将读取该文件的内容并作为内联样式添加。
     *
     * @param options 样式标签的配置选项，包括url、path或content属性
     * @return 插入的样式元素的句柄
     * @throws IOException       当读取样式文件时可能抛出的IO异常
     * @throws EvaluateException 当在页面上执行JavaScript时发生错误时抛出的异常
     */
    public ElementHandle addStyleTag(FrameAddStyleTagOptions options) throws IOException, EvaluateException {
        if (options == null) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        if (StringUtil.isEmpty(options.getUrl()) && StringUtil.isEmpty(options.getPath()) && StringUtil.isEmpty(options.getContent())) {
            throw new JvppeteerException("Provide an object with a `url`, `path` or `content` property");
        }
        String content;
        if (StringUtil.isNotEmpty(options.getPath())) {
            List<String> contents = Files.readAllLines(Paths.get(options.getPath()), StandardCharsets.UTF_8);
            content = String.join("\n", contents) + "/*# sourceURL=" + options.getPath().replaceAll("\n", "") + "*/";
            options.setContent(content);
        }
        return this.mainRealm().transferHandle(this.isolatedRealm().evaluateHandle("async ({url, content}) => {\n" +
                "    return await new Promise(\n" +
                "      (resolve, reject) => {\n" +
                "        let element;\n" +
                "        if (!url) {\n" +
                "          element = document.createElement('style');\n" +
                "          element.appendChild(document.createTextNode(content));\n" +
                "        } else {\n" +
                "          const link = document.createElement('link');\n" +
                "          link.rel = 'stylesheet';\n" +
                "          link.href = url;\n" +
                "          element = link;\n" +
                "        }\n" +
                "        element.addEventListener(\n" +
                "          'load',\n" +
                "          () => {\n" +
                "            resolve(element);\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        element.addEventListener(\n" +
                "          'error',\n" +
                "          event => {\n" +
                "            reject(\n" +
                "              new Error(\n" +
                "                (event ).message ?? 'Could not load style'\n" +
                "              )\n" +
                "            );\n" +
                "          },\n" +
                "          {once: true}\n" +
                "        );\n" +
                "        document.head.appendChild(element);\n" +
                "        return element;\n" +
                "      }\n" +
                "    );\n" +
                "  }", Collections.singletonList(options))).asElement();
    }

    /**
     * 单击找到的第一个与 selector 匹配的元素。
     *
     * @param selector 选择器
     * @param options  （可选的）配置
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void click(String selector, ClickOptions options) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        Objects.requireNonNull(handle, "No node found for selector: " + selector);
        handle.click(options);
        handle.dispose();
    }

    /**
     * 聚焦与 selector 匹配的第一个元素。
     *
     * @param selector 选择器
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void focus(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.focus();
        handle.dispose();
    }

    /**
     * 将指针悬停在与 selector 匹配的第一个元素的中心上
     *
     * @param selector 选择器
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void hover(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.hover();
        handle.dispose();
    }

    /**
     * 在第一个 select 元素上选择与 selector 匹配的一组值。
     *
     * @param selector 选择器
     * @param values   要选择的值的数组。如果 <select> 具有 multiple 属性，则考虑所有值，否则仅考虑第一个值。
     * @return 成功选择的值的列表。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public List<String> select(String selector, List<String> values) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        List<String> result = handle.select(values);
        handle.dispose();
        return result;
    }

    /**
     * 点击与 selector 匹配的第一个元素。
     *
     * @param selector 选择器
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void tap(String selector) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.tap();
        handle.dispose();
    }

    /**
     * 为文本中的每个字符发送 keydown、keypress/input 和 keyup 事件。
     *
     * @param selector 选择器
     * @param text     要输入到元素中的文本
     * @param delay    它设置按键之间等待的时间（以毫秒为单位）。默认为 0。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public void type(String selector, String text, long delay) throws JsonProcessingException, EvaluateException {
        ElementHandle handle = this.$(selector);
        ValidateUtil.assertArg(handle != null, "No node found for selector: " + selector);
        handle.type(text, delay);
        handle.dispose();
    }

    /**
     * 框架的标题。
     *
     * @return 框架的标题。
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS 函数执行异常
     */
    public String title() throws JsonProcessingException, EvaluateException {
        return (String) this.isolatedRealm().evaluate("() => {\n" +
                "      return document.title;\n" +
                "    }", null);
    }

    /**
     * 此方法通常与从 api（如 WebBluetooth）触发 devicerequest 的操作结合使用。
     *
     * @param timeout 超时时间
     * @return DeviceRequestPrompt
     */
    public abstract DeviceRequestPrompt waitForDevicePrompt(int timeout);

    public static List<JSHandle> transposeIterableHandle(JSHandle list) throws JsonProcessingException {
        JSHandle generatorHandle = null;
        try {
            generatorHandle = list.evaluateHandle("iterable => {\n" +
                    "    return (async function* () {\n" +
                    "      yield* iterable;\n" +
                    "    })();\n" +
                    "  }");
            return transposeIteratorHandle(generatorHandle);
        } finally {
            Optional.ofNullable(generatorHandle).ifPresent(JSHandle::dispose);
        }
    }

    public static List<JSHandle> transposeIteratorHandle(JSHandle iterator) throws JsonProcessingException, EvaluateException {
        int size = DEFAULT_BATCH_SIZE;
        List<JSHandle> results = new ArrayList<>();
        List<JSHandle> result;
        while ((result = fastTransposeIteratorHandle(iterator, size)) != null) {
            results.addAll(result);
            size <<= 1;
        }
        return results;
    }

    public static List<JSHandle> fastTransposeIteratorHandle(JSHandle iterator, int size) throws JsonProcessingException, EvaluateException {
        JSHandle array = null;
        Collection<JSHandle> handles;
        try {
            array = iterator.evaluateHandle("async (iterator, size) => {\n" +
                    "    const results = [];\n" +
                    "    while (results.length < size) {\n" +
                    "      const result = await iterator.next();\n" +
                    "      if (result.done) {\n" +
                    "        break;\n" +
                    "      }\n" +
                    "      results.push(result.value);\n" +
                    "    }\n" +
                    "    return results;\n" +
                    "  }", Collections.singletonList(size));
            Map<String, JSHandle> properties = array.getProperties();
            handles = properties.values();
            if (properties.isEmpty()) {
                return null;
            }
            return new ArrayList<>(handles);
        } finally {
            Optional.ofNullable(array).ifPresent(JSHandle::dispose);
        }
    }


    public String id() {
        return id;
    }

    public String parentId() {
        return this.parentId;
    }
}
