package com.ruiyun.jvppeteer.api.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.bidi.entities.PrintMarginParameters;
import com.ruiyun.jvppeteer.cdp.entities.AutofillData;
import com.ruiyun.jvppeteer.cdp.entities.BoundingBox;
import com.ruiyun.jvppeteer.cdp.entities.BoxModel;
import com.ruiyun.jvppeteer.cdp.entities.ClickOptions;
import com.ruiyun.jvppeteer.cdp.entities.DragData;
import com.ruiyun.jvppeteer.cdp.entities.ElementScreenshotOptions;
import com.ruiyun.jvppeteer.cdp.entities.EvaluateType;
import com.ruiyun.jvppeteer.cdp.entities.KeyPressOptions;
import com.ruiyun.jvppeteer.cdp.entities.KeyboardTypeOptions;
import com.ruiyun.jvppeteer.cdp.entities.Offset;
import com.ruiyun.jvppeteer.cdp.entities.Point;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.cdp.entities.ScreenshotClip;
import com.ruiyun.jvppeteer.cdp.entities.WaitForSelectorOptions;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.LazyArg;
import com.ruiyun.jvppeteer.common.QuerySelector;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import com.ruiyun.jvppeteer.util.GetQueryHandler;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import static com.ruiyun.jvppeteer.util.Helper.withSourcePuppeteerURLIfNone;

public abstract class ElementHandle extends JSHandle {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ElementHandle.class);
    //隔离的JSHandle
    protected volatile ElementHandle isolatedHandle;
    protected final JSHandle handle;

    public ElementHandle(JSHandle handle) {
        super();
        this.handle = handle;
    }

    /**
     * 获取隔离的ElementHandle,确保返回的ElementHandle对应PUPPETEER_WORLD类型的IsolatedWorld。
     * <p>
     * 当当前句柄在其frame的隔离realm中时，直接返回自身
     * 否则，如果尚未创建隔离句柄，则在同步块中创建隔离句柄
     * 这确保了在多线程环境下，isolatedHandle的创建是线程安全的
     *
     * @return 当前句柄或其隔离句柄
     * @throws JsonProcessingException 如果转换过程出现错误
     */
    protected ElementHandle adoptIsolatedHandle() throws JsonProcessingException {
        if (this.realm() == this.frame().isolatedRealm()) {
            return this;
        }
        if (this.isolatedHandle == null) {
            synchronized (this) {
                if (this.isolatedHandle == null) {
                    this.isolatedHandle = this.frame().isolatedRealm().adoptHandle(this).asElement();
                }
            }
        }
        return this.isolatedHandle;
    }

    /**
     * 转换由隔离的isolatedHandle执行的结果，转为当前elementHandle的结果
     *
     * @param isolatedResult isolatedHandle执行的结果
     * @return T
     * @throws JsonProcessingException 序列化错误
     */
    @SuppressWarnings("unchecked")
    public <T> T adoptResult(T isolatedResult) throws JsonProcessingException {
        if (isolatedResult == null) {
            return null;
        }
        if (isolatedResult == this.isolatedHandle) {
            return (T) this;
        }
        if (isolatedResult instanceof JSHandle) {
            return (T) this.realm().transferHandle((JSHandle) isolatedResult);
        }

        if (isolatedResult.getClass().isArray()) {
            Object[] resultArray = new Object[Array.getLength(isolatedResult)];
            for (int i = 0; i < Array.getLength(isolatedResult); i++) {
                Object item = Array.get(isolatedResult, i);
                if (item instanceof JSHandle) {
                    try {
                        resultArray[i] = this.realm().transferHandle((JSHandle) item);
                    } catch (JsonProcessingException e) {
                        resultArray[i] = item;
                    }
                } else {
                    resultArray[i] = item;
                }
            }
            return (T) resultArray;
        }
        if (isolatedResult instanceof Collection<?>) {
            List<Object> resultList = new ArrayList<>();
            for (Object item : (Collection<?>) isolatedResult) {
                if (item instanceof JSHandle) {
                    try {
                        resultList.add(this.realm().transferHandle((JSHandle) item));
                    } catch (JsonProcessingException e) {
                        resultList.add(item);
                    }
                } else {
                    resultList.add(item);
                }
            }
            return (T) resultList;
        }
        // 处理 Map 情况
        if (isolatedResult instanceof Map<?, ?>) {
            Map<Object, Object> resultMap = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) isolatedResult).entrySet()) {
                Object value = entry.getValue();
                if (value instanceof JSHandle) {
                    try {
                        value = this.realm().transferHandle((JSHandle) value);
                    } catch (JsonProcessingException e) {
                        // 处理异常
                        // 或者记录日志并继续处理其他条目
                        LOGGER.error("jvppeteer error: ", e);
                    }
                }
                resultMap.put(entry.getKey(), value);
            }
            return (T) resultMap;
        }
        return isolatedResult;
    }

    /**
     * 返回 当前对象的 remoteObject 对象的id
     *
     * @return 当前对象的 remoteObject 对象的id
     */
    @Override
    public String id() {
        return this.handle.id();
    }

    /**
     * 当前对象是否已经释放
     *
     * @return true 代表释放
     */
    @Override
    public boolean disposed() {
        return this.handle.disposed();
    }

    /**
     * 获取表示当前对象属性的句柄映射。
     *
     * @return 返回 JSHandle 的实例
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    @Override
    public JSHandle getProperty(String propertyName) throws JsonProcessingException, EvaluateException {
        return this.adoptResult(this.adoptIsolatedHandle().handle().getProperty(propertyName));
    }

    /**
     * 获取表示当前对象属性的句柄映射。
     *
     * @return 返回 JSHandle 的实例 的集合
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       给定函数执行的异常
     */
    @Override
    public Map<String, JSHandle> getProperties() throws JsonProcessingException {
        return this.adoptResult(this.adoptIsolatedHandle().handle().getProperties());
    }

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
        return this.handle.evaluate(pptrFunction, args);
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
    @Override
    public JSHandle evaluateHandle(String pptrFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pptrFunction = withSourcePuppeteerURLIfNone("evaluateHandle", pptrFunction);
        return this.handle.evaluateHandle(pptrFunction, args);
    }

    /**
     * 表示引用对象的可序列化部分的普通对象
     *
     * @return 给定函数执行的结果
     * @throws JsonProcessingException 如果对象由于循环而无法序列化，则抛出该异常。
     * @throws EvaluateException       给定函数执行的异常
     */
    @Override
    public Object jsonValue() throws JsonProcessingException, EvaluateException {
        return this.adoptResult(this.adoptIsolatedHandle().handle().jsonValue());
    }

    /**
     * 返回 JSHandle 的字符串表示形式。
     *
     * @return JSHandle 的字符串表示形式。
     */
    @Override
    public String toString() {
        return this.handle.toString();
    }

    /**
     * 提供对支持当前对象的 Protocol.Runtime.RemoteObject 的访问。
     *
     * @return 当前对象的 Protocol.Runtime.RemoteObject
     */
    public RemoteObject remoteObject() {
        return this.handle.remoteObject();
    }

    /**
     * 释放当前对象
     * <p>
     * 当页面被重新导航或者关闭，ElementHandles 会被自动处理。
     */
    @Override
    public void dispose() {
        this.handle.dispose();
        if(Objects.nonNull(this.isolatedHandle)){
            this.isolatedHandle.dispose();
        }
    }

    /**
     * 将 JSHandle 转成 ElementHandle 的方法
     * <p>
     * 如果对象是 JSHandle 返回 null,如果对象是 ElementHandle，返回当前对象
     *
     * @return 当前实例
     */
    public ElementHandle asElement() {
        return this;
    }

    /**
     * 与当前对象对应的 Frame
     *
     * @return Frame实例
     */
    public abstract Frame frame();

    /**
     * 查询当前元素内与给定选择器匹配的第一个元素。
     *
     * @param selector 选择器
     * @return 与给定选择器匹配的第一个元素
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS函数执行异常
     */
    public ElementHandle $(String selector) throws JsonProcessingException, EvaluateException {
        QuerySelector queryHandlerAndSelector = GetQueryHandler.getQueryHandlerAndSelector(selector,this.frame());
        JSHandle handle = this.adoptIsolatedHandle().evaluateHandle(queryHandlerAndSelector.getQueryHandler().querySelector(), Arrays.asList(queryHandlerAndSelector.getUpdatedSelector(), new LazyArg()));
        ElementHandle element = handle.asElement();
        if (Objects.nonNull(element)) {
            return this.adoptResult(element);
        }
        return null;
    }

    /**
     * 查询当前元素内与给定选择器匹配的所有元素。
     *
     * @param selector 选择器
     * @return 与给定选择器匹配的所有元素
     * @throws JsonProcessingException 序列化异常
     * @throws EvaluateException       JS函数执行异常
     */
    public List<ElementHandle> $$(String selector) throws JsonProcessingException, EvaluateException {
        QuerySelector queryHandlerAndSelector = GetQueryHandler.getQueryHandlerAndSelector(selector,this.frame());
       return this.adoptResult(queryHandlerAndSelector.getQueryHandler().queryAll(this.adoptIsolatedHandle(), queryHandlerAndSelector.getUpdatedSelector()));
    }


    /**
     * 在当前元素中找到指定选择器的第一个元素，并运行给定的 JS 函数。当前对象是给定 JS 函数的第一个参数
     * <p>
     * 可以用来获取元素内的属性，修改元素属性等
     *
     * @param selector     在当前元素内查找元素的选择器
     * @param pptrFunction 给定的函数
     * @return 找到的第一个元素的ElementHandle
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Object $eval(String selector, String pptrFunction) throws JsonProcessingException, EvaluateException {
        return this.$eval(selector, pptrFunction, null);
    }

    /**
     * 在当前元素中找到指定选择器的第一个元素，并运行给定的 JS 函数。当前对象是给定 JS 函数的第一个参数
     * <p>
     * 可以用来获取元素内的属性，修改元素属性等
     *
     * @param selector     在当前元素内查找元素的选择器
     * @param pptrFunction 给定的  JS 函数
     * @param args         pptrFunction 的参数，如果有的话
     * @return 找到的第一个元素的ElementHandle
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Object $eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pptrFunction = withSourcePuppeteerURLIfNone("$eval", pptrFunction);
        ElementHandle elementHandle = this.$(selector);
        if (elementHandle == null)
            throw new JvppeteerException("Error: failed to find element matching selector " + selector);
        Object result = elementHandle.evaluate(pptrFunction, args);
        elementHandle.dispose();
        return result;
    }

    /**
     * 对当前元素中找到所有与给定选择器匹配的元素，并运行给定的 JS 函数。当前对象是给定 JS 函数的第一个参数
     *
     * @param selector     在当前元素内查找元素的选择器
     * @param pptrFunction 给定的 JS 函数
     * @return 找到的所有元素的 ElementHandle 列表
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Object $$eval(String selector, String pptrFunction) throws JsonProcessingException, EvaluateException {
        return this.$$eval(selector, pptrFunction, null);
    }

    /**
     * 对当前元素中找到所有与给定选择器匹配的元素，并运行给定的 JS 函数。当前对象是给定 JS 函数的第一个参数
     * <p>
     * 与 $eval 函数的区别：可以同时对匹配的所有元素进行操作
     *
     * @param selector     在当前元素内查找元素的选择器
     * @param pptrFunction 给定的 JS 函数
     * @param args         pptrFunction 的参数，如果有的话
     * @return 找到的所有元素的ElementHandle列表
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Object $$eval(String selector, String pptrFunction, List<Object> args) throws JsonProcessingException, EvaluateException {
        pptrFunction = withSourcePuppeteerURLIfNone("$$eval", pptrFunction);
        List<ElementHandle> results = this.$$(selector);
        JSHandle elements = this.evaluateHandle("(_, ...elements) => {\n" +
                "        return elements;\n" +
                "      }", new ArrayList<>(results));
        Object result = elements.evaluate(pptrFunction,args);
        results.forEach(ElementHandle::dispose);
        elements.dispose();
        return result;
    }

    /**
     * 检查可见性
     *
     * @param visibility 要检查可见性 true代表可见，false 代表不可见
     * @return 是否可见
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    private boolean checkVisibility(boolean visibility) throws JsonProcessingException, EvaluateException {
        List<Object> args = new ArrayList<>();
        args.add(new LazyArg());
        args.add(visibility);
        return (boolean) this.evaluate("async (element, PuppeteerUtil, visibility) => {\n" +
                "        return Boolean(PuppeteerUtil.checkVisibility(element, visibility));\n" +
                "      }", args);

    }

    /**
     * 当前元素是否可见
     *
     * @return true 代表可见
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public boolean isVisible() throws JsonProcessingException, EvaluateException {
        return this.adoptIsolatedHandle().checkVisibility(true);
    }

    /**
     * 当前元素是否隐藏
     *
     * @return true 代表隐藏
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public boolean isHidden() throws JsonProcessingException, EvaluateException {
        return this.adoptIsolatedHandle().checkVisibility(false);
    }

    /**
     * 将当前句柄转换为给定的元素类型。
     * <p>
     * 如果句柄不匹配，则会出现错误。句柄不会被自动处置。
     *
     * @param tagName 给定的元素类型
     * @return 转换后的元素
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public ElementHandle toElement(String tagName) throws JsonProcessingException, EvaluateException {
        boolean isMatchingTagName = (boolean) this.adoptIsolatedHandle().evaluate("(node, tagName) => {\n" +
                "      return node.nodeName === tagName.toUpperCase();\n" +
                "    }", Collections.singletonList(tagName));
        if (!isMatchingTagName) {
            throw new JvppeteerException("Element is not a(n) " + tagName + " element");
        }
        return this;
    }

    /**
     * 解析与元素关联的框架（如果有）。HTMLIFrameElements 始终存在。
     *
     * @return Frame
     */
    public abstract Frame contentFrame() throws JsonProcessingException;

    /**
     * 返回元素内的中点，除非提供了特定的偏移量。
     *
     * @return 元素内的中点
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Point clickablePoint() throws JsonProcessingException, EvaluateException {
        return this.clickablePoint(null);
    }

    /**
     * 返回元素内的中点，除非提供了特定的偏移量。
     *
     * @param offset 特定的偏移量。
     * @return 元素内的中点
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public Point clickablePoint(Offset offset) throws JsonProcessingException, EvaluateException {
        BoundingBox box = this.adoptIsolatedHandle().clickableBox();
        if (box == null) {
            throw new JvppeteerException("Node is either not clickable or not an Element");
        }
        if (offset != null) {
            return new Point(box.getX() + offset.getX(), box.getY() + offset.getY());
        }
        return new Point(box.getX() + (box.getWidth() / 2), box.getY() + box.getHeight() / 2);
    }

    private BoundingBox clickableBox() throws JsonProcessingException, EvaluateException {
        Object boxes = this.evaluate("element => {\n" +
                "      if (!(element instanceof Element)) {\n" +
                "        return null;\n" +
                "      }\n" +
                "      return [...element.getClientRects()].map(rect => {\n" +
                "        return {x: rect.x, y: rect.y, width: rect.width, height: rect.height};\n" +
                "      });\n" +
                "    }");

        if (boxes == null) {
            return null;
        }
        ArrayList<BoundingBox> boundingBoxes = Constant.OBJECTMAPPER.readValue(Constant.OBJECTMAPPER.writeValueAsString(boxes), new TypeReference<ArrayList<BoundingBox>>() {
        });
        if (boundingBoxes.isEmpty()) {
            return null;
        }
        this.intersectBoundingBoxesWithFrame(boundingBoxes);
        Frame frame = this.frame();
        Frame parentFrame;
        while ((parentFrame = frame.parentFrame()) != null) {
            ElementHandle elementHandle = frame.frameElement();
            if (elementHandle == null) {
                throw new JvppeteerException("Unsupported frame type");
            }
            try {
                Object response = elementHandle.evaluate("element => {\n" +
                        "        // Element is not visible.\n" +
                        "        if (element.getClientRects().length === 0) {\n" +
                        "          return null;\n" +
                        "        }\n" +
                        "        const rect = element.getBoundingClientRect();\n" +
                        "        const style = window.getComputedStyle(element);\n" +
                        "        return {\n" +
                        "          left:\n" +
                        "            rect.left +\n" +
                        "            parseInt(style.paddingLeft, 10) +\n" +
                        "            parseInt(style.borderLeftWidth, 10),\n" +
                        "          top:\n" +
                        "            rect.top +\n" +
                        "            parseInt(style.paddingTop, 10) +\n" +
                        "            parseInt(style.borderTopWidth, 10),\n" +
                        "        };\n" +
                        "      }");
                if (response == null) {
                    return null;
                }
                PrintMarginParameters parentBox = Constant.OBJECTMAPPER.convertValue(response, PrintMarginParameters.class);
                for (BoundingBox box : boundingBoxes) {
                    box.setX(box.getX() + parentBox.getLeft());
                    box.setY(box.getY() + parentBox.getTop());
                }
                elementHandle.intersectBoundingBoxesWithFrame(boundingBoxes);
                frame = parentFrame;
            } finally {
                elementHandle.dispose();
            }
        }
        Optional<BoundingBox> result = boundingBoxes.stream().filter(box -> box.getWidth() >= 1 && box.getHeight() >= 1).findFirst();
        return result.map(boundingBox -> new BoundingBox(boundingBox.getX(), boundingBox.getY(), boundingBox.getWidth(), boundingBox.getHeight())).orElse(null);
    }

    /**
     * 如果元素在当前视口中可见，则解析为 true。如果一个元素是 SVG，我们会检查 svg 所有者元素是否在视口中。参见 <a href="https://crbug.com/963246">说明</a>。
     *
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    private void intersectBoundingBoxesWithFrame(List<BoundingBox> boundingBoxes) throws JsonProcessingException, EvaluateException {
        Object response = this.frame().isolatedRealm().evaluate("() => {\n" +
                "        return {\n" +
                "          documentWidth: document.documentElement.clientWidth,\n" +
                "          documentHeight: document.documentElement.clientHeight,\n" +
                "        };\n" +
                "      }", null);
        JsonNode responseNode = Constant.OBJECTMAPPER.readTree(Constant.OBJECTMAPPER.writeValueAsString(response));
        for (BoundingBox box : boundingBoxes) {
            intersectBoundingBox(box, responseNode.get("documentWidth").asDouble(), responseNode.get("documentHeight").asDouble());
        }
    }

    private void intersectBoundingBox(BoundingBox box, double width, double height) {
        box.setWidth(Math.max(box.getX() >= 0 ? Math.min(width - box.getX(), box.getWidth()) : Math.min(width, box.getWidth() + box.getX()), 0));
        box.setHeight(Math.max(box.getY() >= 0 ? Math.min(height - box.getY(), box.getHeight()) : Math.min(height, box.getHeight() + box.getY()), 0));
    }

    /**
     * 如果需要，此方法将元素滚动到视图中，然后使用 Page.mouse 将鼠标悬停在元素的中心上。如果元素与 DOM 分离，该方法会抛出错误。
     *
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public void hover() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Point clickablePoint = wrapThis.clickablePoint();
        wrapThis.frame().page().mouse().move(clickablePoint.getX(), clickablePoint.getY());
    }

    /**
     * 如果需要，此方法将元素滚动到视图中，然后使用 Page.mouse 单击元素的中心。如果元素与 DOM 分离，该方法会抛出错误
     *
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public void click() throws JsonProcessingException, EvaluateException {
        this.click(new ClickOptions());
    }

    /**
     * 如果需要，此方法将元素滚动到视图中，然后使用 Page.mouse 单击元素的中心。如果元素与 DOM 分离，该方法会抛出错误
     *
     * @param options 可选的点击参数
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public void click(ClickOptions options) throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Point point = wrapThis.clickablePoint(options.getOffset());
        wrapThis.frame().page().mouse().click(point.getX(), point.getY(), options);
    }

    /**
     * 将当前元素拖动到目标元素上
     *
     * @param target 目标元素
     * @return 当启用拖动拦截时，将返回拖动负载。
     * @throws JsonProcessingException 抛出异常
     */
    public DragData drag(ElementHandle target) throws JsonProcessingException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Page page = wrapThis.frame().page();
        if (page.isDragInterceptionEnabled()) {
            Point source = wrapThis.clickablePoint();
            Point point = target.clickablePoint();
            return page.mouse().drag(source, point);
        }
        try {
            if (!page.isDragging()) {
                page.setDragging(true);
                wrapThis.hover();
                page.mouse().down();
            }
            target.hover();
        } catch (JsonProcessingException | EvaluateException e) {
            page.setDragging(false);
            throw e;
        }
        return null;
    }

    /**
     * 将当前元素拖动到指定位置
     *
     * @param point 指定位置
     * @return 当启用拖动拦截时，将返回拖动负载。
     * @throws JsonProcessingException 抛出异常
     */
    public DragData drag(Point point) throws JsonProcessingException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        Page page = wrapThis.frame().page();
        if (page.isDragInterceptionEnabled()) {
            Point source = wrapThis.clickablePoint();
            return page.mouse().drag(source, point);
        }
        try {
            if (!page.isDragging()) {
                page.setDragging(true);
                wrapThis.hover();
                page.mouse().down();
            }
            page.mouse().move(point.getX(), point.getY());
        } catch (JsonProcessingException | EvaluateException e) {
            page.setDragging(false);
            throw e;
        }
        return null;
    }

    /**
     * 根据给定的值列表来选择元素，并返回选中的值列表
     * 该方法通过在浏览器环境中执行一段JavaScript代码来实现对HTML选择框的操作
     * 包括单选和多选情况的处理，并确保触发相应的输入和变化事件
     *
     * @param values 要选中的值的列表
     * @return 选中的值的列表
     * @throws JsonProcessingException 如果JavaScript代码序列化失败
     * @throws EvaluateException       如果在浏览器环境中执行JavaScript代码失败
     */
    @SuppressWarnings("unchecked")
    public List<String> select(List<String> values) throws JsonProcessingException, EvaluateException {
        /*
         * its evaluate function is properly typed with generics we can
         * return here and remove the typecasting
         */
        String pptrFunction = "(element, vals) => {\n" +
                "      const values = new Set(vals);\n" +
                "      if (!(element instanceof HTMLSelectElement)) {\n" +
                "        throw new Error('Element is not a <select> element.');\n" +
                "      }\n" +
                "\n" +
                "      const selectedValues = new Set();\n" +
                "      if (!element.multiple) {\n" +
                "        for (const option of element.options) {\n" +
                "          option.selected = false;\n" +
                "        }\n" +
                "        for (const option of element.options) {\n" +
                "          if (values.has(option.value)) {\n" +
                "            option.selected = true;\n" +
                "            selectedValues.add(option.value);\n" +
                "            break;\n" +
                "          }\n" +
                "        }\n" +
                "      } else {\n" +
                "        for (const option of element.options) {\n" +
                "          option.selected = values.has(option.value);\n" +
                "          if (option.selected) {\n" +
                "            selectedValues.add(option.value);\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "      element.dispatchEvent(new Event('input', {bubbles: true}));\n" +
                "      element.dispatchEvent(new Event('change', {bubbles: true}));\n" +
                "      return [...selectedValues.values()];\n" +
                "    }";

        return (List<String>) this.adoptIsolatedHandle().evaluate(pptrFunction, Collections.singletonList(values));
    }

    /**
     * 上传文件方法
     * <p>
     * 该方法负责将提供的文件路径列表上传到特定的输入元素它首先确定输入元素是否支持多文件上传，
     * 然后验证文件路径列表的大小是否符合上传条件接着检查每个文件路径是否可读，如果不可读，则抛出异常
     * <p>
     * 当文件路径列表为空时，该方法通过评估一段脚本来更新输入元素的files属性以模拟用户操作,当列表不为空时，
     * 它会通过DOM API设置文件输入元素的文件列表
     *
     * @param filePaths 文件路径列表
     * @throws JsonProcessingException 当JSON处理失败时抛出
     * @throws EvaluateException       当JavaScript评估失败时抛出
     */
    public abstract void uploadFile(List<String> filePaths) throws JsonProcessingException, EvaluateException;

    public abstract List<ElementHandle> queryAXTree(String name, String role) throws JsonProcessingException;

    protected void scrollIntoViewIfNeeded() throws JsonProcessingException, EvaluateException {
        if (this.isIntersectingViewport(1)) {
            return;
        }
        this.scrollIntoView();
    }

    /**
     * 点击元素，如果元素不可见，则滚动到可见。
     *
     * @throws JsonProcessingException 如果序列化失败
     * @throws EvaluateException       如果执行失败
     */
    public void tap() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Point point = wrapThis.clickablePoint();
        wrapThis.frame().page().touchscreen().tap(point.getX(), point.getY());
    }

    /**
     * 此方法在需要时将元素滚动到视图中，然后在元素的中心启动触摸。
     *
     * @throws JsonProcessingException 如果序列化失败
     * @throws EvaluateException       如果执行失败
     */
    public void touchStart() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Point point = wrapThis.clickablePoint();
        wrapThis.frame().page().touchscreen().touchStart(point.getX(), point.getY(), null);
    }

    /**
     * 此方法在需要时将元素滚动到视图中，然后将触摸移动到元素的中心。
     *
     * @throws JsonProcessingException 如果序列化失败
     * @throws EvaluateException       如果执行失败
     */
    public void touchMove() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        Point point = wrapThis.clickablePoint();
        wrapThis.frame().page().touchscreen().touchMove(point.getX(), point.getY());
    }

    /**
     * 结束触摸元素
     *
     * @throws JsonProcessingException 如果序列化失败
     * @throws EvaluateException       如果 JS 函数执行失败
     */
    public void touchEnd() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.scrollIntoViewIfNeeded();
        wrapThis.frame().page().touchscreen().touchEnd();
    }

    /**
     * 对元素调用 focus。
     *
     * @throws JsonProcessingException Json解析异常
     * @throws EvaluateException       如果 JS 函数执行失败
     */
    public void focus() throws JsonProcessingException, EvaluateException {
        this.adoptIsolatedHandle().evaluate("element => {\n" +
                "      if (!(element instanceof HTMLElement)) {\n" +
                "        throw new Error('Cannot focus non-HTMLElement');\n" +
                "      }\n" +
                "      return element.focus();\n" +
                "    }");
    }

    /**
     * 聚焦元素，然后为文本中的每个字符发送 keydown、keypress/input 和 keyup 事件。
     * <p>
     * 要按特殊键，例如 Control 或 ArrowDown，请使用 ElementHandle.press()。
     *
     * @param text 发送的文本
     * @throws JsonProcessingException Json解析异常
     * @throws EvaluateException       如果 JS 函数执行失败
     */
    public void type(String text) throws JsonProcessingException, EvaluateException {
        this.type(text, 0);
    }

    /**
     * 聚焦元素，然后为文本中的每个字符发送 keydown、keypress/input 和 keyup 事件。
     * <p>
     * 要按特殊键，例如 Control 或 ArrowDown，请使用 ElementHandle.press()。
     *
     * @param text  发送的文本
     * @param delay 间隔 delay 毫秒数发送文本的字符数
     * @throws JsonProcessingException Json解析异常
     * @throws EvaluateException       如果 JS 函数执行失败
     */
    public void type(String text, long delay) throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.focus();
        KeyboardTypeOptions options = new KeyboardTypeOptions();
        options.setDelay(delay);
        wrapThis.frame().page().keyboard().type(text, options);
    }

    /**
     * 聚焦元素，然后使用 Keyboard.down() 和 Keyboard.up()。
     * <p>
     * 如果 key 是单个字符，并且除了 Shift 之外没有按下修饰键，则还将生成 keypress/input 事件。可以指定 text 选项来强制生成输入事件。
     * <p>
     * 注意修饰键确实会影响 elementHandle.press。按住 Shift 将以大写形式键入文本。
     * </p>
     *
     * @param key 要按下的键
     */
    public void press(String key) throws JsonProcessingException, EvaluateException {
        this.press(key, new KeyPressOptions());
    }

    /**
     * 聚焦元素，然后使用 Keyboard.down() 和 Keyboard.up()。
     *
     * @param key     要按下的键
     * @param options 选项
     */
    public void press(String key, KeyPressOptions options) throws JsonProcessingException, EvaluateException {
        this.adoptIsolatedHandle().focus();
        this.adoptIsolatedHandle().frame().page().keyboard().press(key, options);
    }

    /**
     * 将当前元素滚动到视图中。此方法通过自动化协议客户端或调用 element.scrollIntoView 方法来实现。
     * 它确保元素的中心位置在浏览器视图的中心。
     *
     * @throws JsonProcessingException 当处理JSON数据时发生错误。
     * @throws EvaluateException       当执行脚本评估时发生错误。
     */
    public void scrollIntoView() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.assertConnectedElement();
        wrapThis.evaluate("async (element) => {\n" +
                "      element.scrollIntoView({\n" +
                "        block: 'center',\n" +
                "        inline: 'center',\n" +
                "        behavior: 'instant',\n" +
                "      });\n" +
                "    }");
    }

    protected void assertConnectedElement() throws JsonProcessingException, EvaluateException {
        Object error = this.evaluate("async element => {\n" +
                "      if (!element.isConnected) {\n" +
                "        return 'Node is detached from document';\n" +
                "      }\n" +
                "      if (element.nodeType !== Node.ELEMENT_NODE) {\n" +
                "        return 'Node is not of type HTMLElement';\n" +
                "      }\n" +
                "      return;\n" +
                "    }");
        if (error != null) {
            throw new JvppeteerException((String) error);
        }
    }

    /**
     * 如果元素在当前视口中可见，则解析为 true。如果一个元素是 SVG，我们会检查 svg 所有者元素是否在视口中。参见 <a href="https://crbug.com/963246">说明</a>。
     *
     * @param threshold 0（无交叉）和 1（完全交叉）之间交叉的阈值。默认为 1。
     * @return 返回元素在当前视口中是否可见
     * @throws JsonProcessingException Json解析异常
     * @throws EvaluateException       评估异常
     */
    public boolean isIntersectingViewport(int threshold) throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        wrapThis.assertConnectedElement();
        ElementHandle svgelEmentHandle = wrapThis.asSVGElementHandle();
        ElementHandle target = null;
        try {
            if (svgelEmentHandle != null) {
                target = svgelEmentHandle.getOwnerSVGElement();
            }
            String pptrFunction = "async (element, threshold) => {\n" +
                    "        const visibleRatio = await new Promise(resolve => {\n" +
                    "          const observer = new IntersectionObserver(entries => {\n" +
                    "            resolve(entries[0].intersectionRatio);\n" +
                    "            observer.disconnect();\n" +
                    "          });\n" +
                    "          observer.observe(element);\n" +
                    "        });\n" +
                    "        return threshold === 1 ? visibleRatio === 1 : visibleRatio > threshold;\n" +
                    "      }";
            if (target == null) {
                return (boolean) wrapThis.evaluate(pptrFunction, Collections.singletonList(threshold));
            } else {
                return (boolean) target.evaluate(pptrFunction, Collections.singletonList(threshold));
            }
        } finally {
            if (target != null) {
                target.dispose();
            }
        }
    }

    /**
     * 如果元素在当前视口中可见，则解析为 true。如果一个元素是 SVG，我们会检查 svg 所有者元素是否在视口中。参见 <a href="https://crbug.com/963246">说明</a>。
     *
     * @return 返回元素在当前视口中是否可见
     * @throws JsonProcessingException Json解析异常
     * @throws EvaluateException       评估异常
     */
    public boolean isIntersectingViewport() throws JsonProcessingException, EvaluateException {
        return this.isIntersectingViewport(0);
    }


    private ElementHandle getOwnerSVGElement() throws JsonProcessingException, EvaluateException {
        JSHandle handle = this.evaluateHandle("element => {\n" +
                "      if (element instanceof SVGSVGElement) {\n" +
                "        return element;\n" +
                "      }\n" +
                "      return element.ownerSVGElement;\n" +
                "    }");
        return handle.asElement();
    }

    private ElementHandle asSVGElementHandle() throws JsonProcessingException, EvaluateException {
        boolean response = (boolean) this.evaluate("element => {\n" +
                "        return element instanceof SVGElement;\n" +
                "      }");
        if (response) {
            return this;
        }
        return null;
    }

    /**
     * 如果该元素是表单输入，则可以使用 ElementHandle.autofill() 来测试表单是否与浏览器的自动填充实现兼容。如果无法自动填写表单，则会引发错误。
     * <p>
     * 目前，仅支持自动填充信用卡信息，并且在 Chrome 中仅支持新的 headless 和 headful 模式。
     *
     * @param data 自动填写表单数据
     */
    public abstract void autofill(AutofillData data);

    /**
     * 此方法返回元素的边界框（相对于主框架），如果元素是 不是布局的一部分，则返回 null（例如：display: none).
     *
     * @return 元素的边界框
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public BoundingBox boundingBox() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        Object box = wrapThis.evaluate("element => {\n" +
                "      if (!(element instanceof Element)) {\n" +
                "        return null;\n" +
                "      }\n" +
                "      // Element is not visible.\n" +
                "      if (element.getClientRects().length === 0) {\n" +
                "        return null;\n" +
                "      }\n" +
                "      const rect = element.getBoundingClientRect();\n" +
                "      return {x: rect.x, y: rect.y, width: rect.width, height: rect.height};\n" +
                "    }");
        if (box == null)
            return null;
        Offset offset = wrapThis.getTopLeftCornerOfFrame();
        if (offset == null) {
            return null;
        }
        JsonNode boxNode = Constant.OBJECTMAPPER.readTree(Constant.OBJECTMAPPER.writeValueAsString(box));
        return new BoundingBox(boxNode.get("x").asDouble() + offset.getX(), boxNode.get("y").asDouble() + offset.getY(), boxNode.get("width").asDouble(), boxNode.get("height").asDouble());
    }

    @SuppressWarnings("unchecked")
    private Offset getTopLeftCornerOfFrame() throws JsonProcessingException, EvaluateException {
        Offset point = new Offset();
        Frame frame = this.frame();
        Frame parentFrame;
        while (frame != null && (parentFrame = frame.parentFrame()) != null) {
            ElementHandle elementHandle = frame.frameElement();
            if (elementHandle == null) {
                throw new JvppeteerException("Unsupported frame type");
            }
            LinkedHashMap<String, Integer> parentBox = (LinkedHashMap<String, Integer>) elementHandle.evaluate("element => {\n" +
                    "                            // Element is not visible.\n" +
                    "                            if (element.getClientRects().length === 0) {\n" +
                    "                              return null;\n" +
                    "                            }\n" +
                    "                            const rect = element.getBoundingClientRect();\n" +
                    "                            const style = window.getComputedStyle(element);\n" +
                    "                            return {\n" +
                    "                              left:\n" +
                    "                                rect.left +\n" +
                    "                                parseInt(style.paddingLeft, 10) +\n" +
                    "                                parseInt(style.borderLeftWidth, 10),\n" +
                    "                              top:\n" +
                    "                                rect.top +\n" +
                    "                                parseInt(style.paddingTop, 10) +\n" +
                    "                                parseInt(style.borderTopWidth, 10),\n" +
                    "                            };\n" +
                    "                          }");
            if (parentBox == null) {
                return null;
            }
            point.setX(point.getX() + parentBox.get("left"));
            point.setY(point.getY() + parentBox.get("top"));
            frame = parentFrame;
        }
        return point;
    }

    /**
     * 此方法返回元素的框，如果元素是 不是布局的一部分，则返回 null（例如：display: none）。
     * <p>
     * 盒子被表示为点数组；每个点都是一个对象 {x, y}。箱点按顺时针顺序排序。
     *
     * @return BoxModel
     * @throws JsonProcessingException 当处理JSON时发生错误
     * @throws EvaluateException       当页面函数执行失败时抛出异常
     */
    public BoxModel boxModel() throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        Object response = wrapThis.evaluate("element => {\n" +
                "  if (!(element instanceof Element)) {\n" +
                "    return null;\n" +
                "  }\n" +
                "  // Element is not visible.\n" +
                "  if (element.getClientRects().length === 0) {\n" +
                "    return null;\n" +
                "  }\n" +
                "  const rect = element.getBoundingClientRect();\n" +
                "  const style = window.getComputedStyle(element);\n" +
                "  const offsets = {\n" +
                "    padding: {\n" +
                "      left: parseInt(style.paddingLeft, 10),\n" +
                "      top: parseInt(style.paddingTop, 10),\n" +
                "      right: parseInt(style.paddingRight, 10),\n" +
                "      bottom: parseInt(style.paddingBottom, 10),\n" +
                "    },\n" +
                "    margin: {\n" +
                "      left: -parseInt(style.marginLeft, 10),\n" +
                "      top: -parseInt(style.marginTop, 10),\n" +
                "      right: -parseInt(style.marginRight, 10),\n" +
                "      bottom: -parseInt(style.marginBottom, 10),\n" +
                "    },\n" +
                "    border: {\n" +
                "      left: parseInt(style.borderLeft, 10),\n" +
                "      top: parseInt(style.borderTop, 10),\n" +
                "      right: parseInt(style.borderRight, 10),\n" +
                "      bottom: parseInt(style.borderBottom, 10),\n" +
                "    },\n" +
                "  };\n" +
                "  const border = [\n" +
                "    {x: rect.left, y: rect.top},\n" +
                "    {x: rect.left + rect.width, y: rect.top},\n" +
                "    {x: rect.left + rect.width, y: rect.top + rect.bottom},\n" +
                "    {x: rect.left, y: rect.top + rect.bottom},\n" +
                "  ];\n" +
                "  const padding = transformQuadWithOffsets(border, offsets.border);\n" +
                "  const content = transformQuadWithOffsets(padding, offsets.padding);\n" +
                "  const margin = transformQuadWithOffsets(border, offsets.margin);\n" +
                "  return {\n" +
                "    content,\n" +
                "    padding,\n" +
                "    border,\n" +
                "    margin,\n" +
                "    width: rect.width,\n" +
                "    height: rect.height,\n" +
                "  };\n" +
                "\n" +
                "  function transformQuadWithOffsets(\n" +
                "    quad,\n" +
                "    offsets\n" +
                "  ) {\n" +
                "    return [\n" +
                "      {\n" +
                "        x: quad[0].x + offsets.left,\n" +
                "        y: quad[0].y + offsets.top,\n" +
                "      },\n" +
                "      {\n" +
                "        x: quad[1].x - offsets.right,\n" +
                "        y: quad[1].y + offsets.top,\n" +
                "      },\n" +
                "      {\n" +
                "        x: quad[2].x - offsets.right,\n" +
                "        y: quad[2].y - offsets.bottom,\n" +
                "      },\n" +
                "      {\n" +
                "        x: quad[3].x + offsets.left,\n" +
                "        y: quad[3].y - offsets.bottom,\n" +
                "      },\n" +
                "    ];\n" +
                "  }\n" +
                "}");
        if (response == null) {
            return null;
        }
        Offset offset = wrapThis.getTopLeftCornerOfFrame();
        if (offset == null) {
            return null;
        }
        BoxModel model = Constant.OBJECTMAPPER.readValue(Constant.OBJECTMAPPER.writeValueAsString(response), BoxModel.class);
        model.getContent().forEach(point -> {
            point.setX(point.getX() + offset.getX());
            point.setY(point.getY() + offset.getY());
        });
        model.getPadding().forEach(point -> {
            point.setX(point.getX() + offset.getX());
            point.setY(point.getY() + offset.getY());
        });
        model.getBorder().forEach(point -> {
            point.setX(point.getX() + offset.getX());
            point.setY(point.getY() + offset.getY());
        });
        model.getMargin().forEach(point -> {
            point.setX(point.getX() + offset.getX());
            point.setY(point.getY() + offset.getY());
        });
        return model;
    }

    public String screenshot(String path) throws IOException, EvaluateException, ExecutionException, InterruptedException {
        ElementScreenshotOptions options = new ElementScreenshotOptions();
        options.setPath(path);
        return this.screenshot(options);
    }

    /**
     * 如果需要，此方法将元素滚动到视图中，然后使用 Page.screenshot() 截取元素的屏幕截图。如果元素与 DOM 分离，该方法会抛出错误。
     *
     * @param options 截图配置
     * @return 返回图片路径
     * @throws IOException       IO异常
     * @throws EvaluateException 截图异常
     */
    public String screenshot(ElementScreenshotOptions options) throws IOException, EvaluateException, ExecutionException, InterruptedException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        Page page = wrapThis.frame().page();
        if (options.getScrollIntoView()) {
            this.scrollIntoViewIfNeeded();
        }
        BoundingBox elementClip = wrapThis.nonEmptyVisibleBoundingBox();
        Object arr = wrapThis.evaluate("() => {\n" +
                "      if (!window.visualViewport) {\n" +
                "        throw new Error('window.visualViewport is not supported.');\n" +
                "      }\n" +
                "      return [\n" +
                "        window.visualViewport.pageLeft,\n" +
                "        window.visualViewport.pageTop,\n" +
                "      ];\n" +
                "    }");
        JsonNode arrNode = Constant.OBJECTMAPPER.readTree(Constant.OBJECTMAPPER.writeValueAsString(arr));

        elementClip.setX(elementClip.getX() + arrNode.get(0).asDouble());
        elementClip.setY(elementClip.getY() + arrNode.get(1).asDouble());
        if (options.getClip() != null) {
            elementClip.setX(elementClip.getX() + options.getClip().getX());
            elementClip.setY(elementClip.getY() + options.getClip().getY());
            elementClip.setWidth(options.getClip().getWidth());
            elementClip.setHeight(options.getClip().getHeight());
        }
        options.setClip(new ScreenshotClip(elementClip.getX(), elementClip.getY(), elementClip.getWidth(), elementClip.getHeight(), 1));
        return page.screenshot(options);
    }

    /**
     * 等待直到指定的选择器匹配的元素满足某些条件（可见、隐藏或存在）.
     *
     * @param selector 选择器字符串，用于选择目标元素.
     * @param options  包含等待条件的选项，如元素可见或隐藏.
     * @return 返回匹配选择器的目标元素的句柄.
     */
    public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) throws JsonProcessingException {
        QuerySelector querySelector = GetQueryHandler.getQueryHandlerAndSelector(selector,this.frame());
        options.setPolling(querySelector.getPolling());
        return querySelector.getQueryHandler().waitFor(this.adoptIsolatedHandle(), querySelector.getUpdatedSelector(), options);
    }

    private BoundingBox nonEmptyVisibleBoundingBox() throws JsonProcessingException, EvaluateException {
        BoundingBox box = this.boundingBox();
        Objects.requireNonNull(box, "Node is either not visible or not an HTMLElement");
        ValidateUtil.assertArg(box.getWidth() != 0, "Node has 0 width.");
        ValidateUtil.assertArg(box.getHeight() != 0, "Node has 0 height.");
        return box;
    }

    private JSHandle handle() {
        return this.handle;
    }
    /**
     * When connected using Chrome DevTools Protocol, it returns a
     * DOM.BackendNodeId for the element.
     */
    public  abstract int backendNodeId();
}
