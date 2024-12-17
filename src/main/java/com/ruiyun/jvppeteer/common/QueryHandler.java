package com.ruiyun.jvppeteer.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.api.core.JSHandle;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.StringUtil;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


import static com.ruiyun.jvppeteer.api.core.Frame.transposeIterableHandle;
import static com.ruiyun.jvppeteer.util.Helper.throwError;

public abstract class QueryHandler {


    public ElementHandle queryOne(ElementHandle element, String selector) throws JsonProcessingException {
        JSHandle handle = element.evaluateHandle(this.querySelector(), Arrays.asList(selector, new LazyArg()));
        if (Objects.isNull(handle)) {
            return null;
        }
        return handle.asElement();
    }

    public List<ElementHandle> queryAll(ElementHandle element, String selector) throws JsonProcessingException {
        JSHandle handle = element.evaluateHandle(this.querySelectorAll(), Arrays.asList(selector, new LazyArg()));
        if (Objects.isNull(handle)) {
            return null;
        }
        List<JSHandle> handles = transposeIterableHandle(handle);
        return handles.stream().map(JSHandle::asElement).filter(Objects::nonNull).collect(java.util.stream.Collectors.toList());
    }

    public abstract String querySelector();

    public abstract String querySelectorAll();


    public ElementHandle waitFor(ElementHandle handle, String selector, WaitForSelectorOptions options) throws JsonProcessingException {
        Frame frame = handle.frame();
        ElementHandle element = frame.isolatedRealm().adoptHandle(handle);
        String polling = options.getVisible() || options.getHidden() ? "raf" : options.getPolling();
        WaitForSelectorOptions waitForSelectorOptions = new WaitForSelectorOptions();
        waitForSelectorOptions.setPolling(polling);
        waitForSelectorOptions.setRoot(element);
        waitForSelectorOptions.setTimeout(options.getTimeout());
        try {
            JSHandle result = frame.isolatedRealm().waitForFunction("async (PuppeteerUtil, query, selector, root, visible) => {\n" +
                    "  const querySelector = PuppeteerUtil.createFunction(\n" +
                    "    query,\n" +
                    "  );\n" +
                    "  const node = await querySelector(\n" +
                    "    root ?? document,\n" +
                    "    selector,\n" +
                    "    PuppeteerUtil,\n" +
                    "  );\n" +
                    "  return PuppeteerUtil.checkVisibility(node, visible);\n" +
                    "}", waitForSelectorOptions, EvaluateType.FUNCTION, new LazyArg(), getQuerySelector(this), selector, element, options.getVisible() ? Boolean.TRUE : options.getHidden() ? Boolean.FALSE : null);
            if (Objects.isNull(result) || Objects.isNull(result.asElement())) {
                return null;
            }
            return frame.mainRealm().transferHandle(result.asElement());
        } catch (Exception e) {
            if (!(e instanceof EvaluateException)) {
                throwError(e);
            }
            EvaluateException error = (EvaluateException) e;
            if (Objects.equals("AbortError", error.getName())) {
                throw error;
            }
            throw new EvaluateException("Waiting for selector " + selector + " failed: ${error.message}",e);
        }
    }

    public ElementHandle waitFor(Frame frame, String selector, WaitForSelectorOptions options) {
        ElementHandle element = null;
        String polling = options.getVisible() || options.getHidden() ? "raf" : options.getPolling();
        WaitForSelectorOptions waitForSelectorOptions = new WaitForSelectorOptions();
        waitForSelectorOptions.setPolling(polling);
        waitForSelectorOptions.setRoot(element);
        waitForSelectorOptions.setTimeout(options.getTimeout());
        try {
            JSHandle handle = frame.isolatedRealm().waitForFunction("async (PuppeteerUtil, query, selector, root, visible) => {\n" +
                    "  const querySelector = PuppeteerUtil.createFunction(\n" +
                    "    query,\n" +
                    "  );\n" +
                    "  const node = await querySelector(\n" +
                    "    root ?? document,\n" +
                    "    selector,\n" +
                    "    PuppeteerUtil,\n" +
                    "  );\n" +
                    "  return PuppeteerUtil.checkVisibility(node, visible);\n" +
                    "}", waitForSelectorOptions, EvaluateType.FUNCTION, new LazyArg(), getQuerySelector(this), selector, null, options.getVisible() ? Boolean.TRUE : options.getHidden() ? Boolean.FALSE : null);
            if (Objects.isNull(handle) || Objects.isNull(handle.asElement())) {
                return null;
            }
            return frame.mainRealm().transferHandle(handle.asElement());
        } catch (Exception e) {
            if (!(e instanceof EvaluateException)) {
                throwError(e);
            }
            EvaluateException error = (EvaluateException) e;
            if (Objects.equals("AbortError", error.getName())) {
                throw error;
            }
            throw new EvaluateException("Waiting for selector " + selector + " failed: ${error.message}",e);
        }
    }

    public static String getQuerySelector(QueryHandler handler) {
        if (StringUtil.isNotEmpty(handler.querySelector())) {
            return handler.querySelector();
        } else if (StringUtil.isNotEmpty(handler.querySelectorAll())) {
            return handler.querySelectorAll();
        } else {
            throw new JvppeteerException("Cannot create default `querySelector`.");
        }
    }

    public static String getQuerySelectorAll(QueryHandler handler) {
        if (StringUtil.isNotEmpty(handler.querySelectorAll())) {
            return handler.querySelectorAll();
        } else if (StringUtil.isNotEmpty(handler.querySelector())) {
            return handler.querySelector();
        } else {
            throw new JvppeteerException("Cannot create default `querySelector`.");
        }
    }

}
