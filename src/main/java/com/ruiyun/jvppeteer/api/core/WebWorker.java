package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.common.TimeoutSettings;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.util.List;


import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public abstract class WebWorker {

    public TimeoutSettings timeoutSettings = new TimeoutSettings();

    protected String url;

    public WebWorker(String url) {
        super();
        this.url = url;
    }

    public abstract Realm mainRealm();

    /**
     * 此 Web Worker 的 URL。
     *
     * @return URL
     */
    public String url() {
        return this.url;
    }

    /**
     * 属于该 WebWorker 的 CDP session client
     */
    public abstract CDPSession client();

    /**
     * 在 worker 中运行一段 JS 函数，返回该 JS 函数的句柄
     * 根据经验，如果给定函数的返回值比 JSON 对象（例如大多数类）更复杂，那么 evaluate _ 可能 _ 返回一些截断值（或 {}）。这是因为我们返回的不是实际的返回值，而是通过协议将返回值传输到 Puppeteer 的结果的反序列化版本。
     * <p>
     * 一般来说，如果 evaluate 无法正确序列化返回值或者你需要一个可变的 handle 作为返回对象，则应该使用 evaluateHandle。
     *
     * @param pptrFunction 要执行的 JavaScript 函数
     * @return pptrFunction 执行结果
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     * @throws JsonProcessingException 如果在序列化返回值时发生错误
     */
    public Object evaluate(String pptrFunction) throws EvaluateException, JsonProcessingException {
        return this.evaluate(pptrFunction, null);
    }

    /**
     * 在 worker 中运行一段 JS 函数,返回 JS 函数运行的结果
     * 根据经验，如果给定函数的返回值比 JSON 对象（例如大多数类）更复杂，那么 evaluate _ 可能 _ 返回一些截断值（或 {}）。这是因为我们返回的不是实际的返回值，而是通过协议将返回值传输到 Puppeteer 的结果的反序列化版本。
     * <p>
     * 一般来说，如果 evaluate 无法正确序列化返回值或者你需要一个可变的 handle 作为返回对象，则应该使用 evaluateHandle。
     *
     * @param pptrFunction 要执行的 JavaScript 函数
     * @return pptrFunction 执行结果
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     * @throws JsonProcessingException 如果在序列化返回值时发生错误
     */
    public JSHandle evaluateHandle(String pptrFunction) throws EvaluateException, JsonProcessingException {
        return this.evaluateHandle(pptrFunction, null);
    }

    /**
     * 在 worker 中运行一段 JS 函数,返回 JS 函数运行的结果
     * 根据经验，如果给定函数的返回值比 JSON 对象（例如大多数类）更复杂，那么 evaluate _ 可能 _ 返回一些截断值（或 {}）。这是因为我们返回的不是实际的返回值，而是通过协议将返回值传输到 Puppeteer 的结果的反序列化版本。
     * <p>
     * 一般来说，如果 evaluate 无法正确序列化返回值或者你需要一个可变的 handle 作为返回对象，则应该使用 evaluateHandle。
     *
     * @param pptrFunction 要执行的 JavaScript 函数
     * @param args         pptrFunction 函数的参数
     * @return pptrFunction 执行结果
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     * @throws JsonProcessingException 如果在序列化返回值时发生错误
     */
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws EvaluateException, JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        return this.mainRealm().evaluateHandle(pptrFunction, args);
    }

    /**
     * 在 worker 中运行一段 JS 函数 ，返回该 JS 函数的句柄
     * 根据经验，如果给定函数的返回值比 JSON 对象（例如大多数类）更复杂，那么 evaluate _ 可能 _ 返回一些截断值（或 {}）。这是因为我们返回的不是实际的返回值，而是通过协议将返回值传输到 Puppeteer 的结果的反序列化版本。
     * <p>
     * 一般来说，如果 evaluate 无法正确序列化返回值或者你需要一个可变的 handle 作为返回对象，则应该使用 evaluateHandle。
     *
     * @param pptrFunction 要执行的 JavaScript 函数
     * @param args         pptrFunction 函数的参数
     * @return pptrFunction 执行结果
     * @throws EvaluateException       如果在浏览器端执行函数时发生错误
     * @throws JsonProcessingException 如果在序列化返回值时发生错误
     */
    public Object evaluate(String pptrFunction, List<Object> args) throws EvaluateException, JsonProcessingException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluate", pptrFunction);
        return this.mainRealm().evaluate(pptrFunction, args);
    }

    public void close() throws EvaluateException, JsonProcessingException {
        throw new JvppeteerException("WebWorker.close() is not supported");
    }
}
