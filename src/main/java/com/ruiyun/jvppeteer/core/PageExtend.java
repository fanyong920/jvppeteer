package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.StringUtil;

import java.util.List;

public class PageExtend {


    public static String html(Page page) throws JsonProcessingException, EvaluateException {
        if (null == page) return null;
        ElementHandle handle = byTag(page, "html");
        if (null == handle) return null;
        JSHandle jsHandle = handle.getProperty("outerHTML");
        if (null == jsHandle) return null;
        return jsHandle.jsonValue().toString();
    }

    public static String text(Page page) throws JsonProcessingException, EvaluateException {
        if (null == page) return null;
        ElementHandle handle = byTag(page, "html");
        if (null == handle) return null;
        JSHandle jsHandle = handle.getProperty("innerText");
        if (null == jsHandle) return null;
        return jsHandle.jsonValue().toString();
    }

    public static ElementHandle byId(Page page, String param) throws EvaluateException, JsonProcessingException {
        if (null == page || StringUtil.isEmpty(param)) {
            return null;
        }
        return page.$("#".concat(param));
    }

    public static ElementHandle byTag(Page page, String param) throws EvaluateException, JsonProcessingException {
        if (null == page || StringUtil.isEmpty(param)) {
            return null;
        }
        return page.$(param);
    }

    public static ElementHandle byClass(Page page, String param) throws EvaluateException, JsonProcessingException {
        if (null == page || StringUtil.isEmpty(param)) {
            return null;
        }
        return page.$(".".concat(param));
    }

    public static List<ElementHandle> byTagList(Page page, String param) throws EvaluateException, JsonProcessingException {
        if (null == page || StringUtil.isEmpty(param)) {
            return null;
        }
        return page.$$(param);
    }

    public static List<ElementHandle> byClassList(Page page, String param) throws EvaluateException, JsonProcessingException {
        if (null == page || StringUtil.isEmpty(param)) {
            return null;
        }
        return page.$$(".".concat(param));
    }

}
