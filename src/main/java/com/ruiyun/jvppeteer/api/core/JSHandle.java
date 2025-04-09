package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public abstract class JSHandle {
    public JSHandle() {
    }

    public abstract Realm realm();

    public abstract boolean disposed();

    /**
     * 使用当前对象作为第一个参数来执行给定的 JS 函数。
     *
     * @param pptrFunction 给定函数
     * @return 给定函数执行的结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    public Object evaluate(String pptrFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluate(pptrFunction, null);
    }

    /**
     * 使用当前对象作为第一个参数来执行给定的 JS 函数。
     *
     * @param pptrFunction 给定函数
     * @param args         给定函数的参数，第一个参数是当前对象
     * @return 给定函数执行的结果
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    public Object evaluate(String pptrFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluate", pptrFunction);
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        if (Objects.nonNull(args)) {
            argsArray.addAll(args);
        }
        return this.realm().evaluate(pptrFunction, argsArray);
    }

    /**
     * 使用当前对象作为第一个参数来执行给定的 JS 函数。
     *
     * @param pptrFunction 给定函数
     * @return 返回 JSHandle 的实例
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    public JSHandle evaluateHandle(String pptrFunction) throws JsonProcessingException, EvaluateException {
        return this.evaluateHandle(pptrFunction, null);
    }

    /**
     * 使用当前对象作为第一个参数来执行给定的 JS 函数。
     *
     * @param pptrFunction 给定函数
     * @param args         给定函数的参数，第一个参数是当前对象
     * @return 返回 JSHandle 的实例
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        List<Object> argsArray = new ArrayList<>();
        argsArray.add(this);
        if (ValidateUtil.isNotEmpty(args)) {
            argsArray.addAll(args);
        }
        return this.realm().evaluateHandle(pptrFunction, argsArray);
    }

    /**
     * 获取表示当前对象属性的句柄映射。
     *
     * @return 返回 JSHandle 的实例
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    public JSHandle getProperty(String propertyName) throws JsonProcessingException, EvaluateException {
        String pptrFunction = "(object, propertyName) => {\n" +
                "      return object[propertyName];\n" +
                "    }";
        return this.evaluateHandle(pptrFunction, Collections.singletonList(propertyName));
    }

    /**
     * 获取表示当前对象属性的句柄映射。
     *
     * @return 返回 JSHandle 的实例 的集合
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    @SuppressWarnings("unchecked")
    public Map<String, JSHandle> getProperties() throws JsonProcessingException {
        List<String> propertyNames = (List<String>) this.evaluate("object => {\n" +
                "      const enumerableProperties = [];\n" +
                "      const descriptors = Object.getOwnPropertyDescriptors(object);\n" +
                "      for (const propertyName in descriptors) {\n" +
                "        if (descriptors[propertyName]?.enumerable) {\n" +
                "          enumerableProperties.push(propertyName);\n" +
                "        }\n" +
                "      }\n" +
                "      return enumerableProperties;\n" +
                "    }");
        Map<String, JSHandle> map = new LinkedHashMap<>(propertyNames.size());
        for (String propertyName : propertyNames) {
            JSHandle handle = this.getProperty(propertyName);
            if (Objects.nonNull(handle)) {
                map.put(propertyName, handle);
            }
        }
        return map;
    }

    /**
     * 表示引用对象的可序列化部分的普通对象
     *
     * @return 给定函数执行的结果
     * @throws JsonProcessingException 如果对象由于循环而无法序列化，则抛出该异常。
     */
    public abstract Object jsonValue() throws JsonProcessingException;

    /**
     * 如果句柄是 ElementHandle 的实例，则为 null 或句柄本身。
     *
     * @return ElementHandle 的实例
     */
    public abstract ElementHandle asElement();

    /**
     * 释放句柄引用的对象以进行垃圾回收。
     */
    public abstract void dispose();

    /**
     * 返回 JSHandle 的字符串表示形式。
     *
     * @return JSHandle 的字符串表示形式。
     */
    public abstract String toString();

    /**
     * 当前对象的 Protocol.Runtime.RemoteObject 的 id。
     *
     * @return id
     */
    public abstract String id();

    /**
     * 提供对支持当前对象的 Protocol.Runtime.RemoteObject 的访问。
     *
     * @return 当前对象的 Protocol.Runtime.RemoteObject
     */
    public abstract RemoteObject remoteObject();
}
