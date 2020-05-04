package com.ruiyun.jvppeteer.types.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;
import com.ruiyun.jvppeteer.options.ClickOptions;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import com.ruiyun.jvppeteer.protocol.PageEvaluateType;
import com.ruiyun.jvppeteer.protocol.input.BoxModel;
import com.ruiyun.jvppeteer.protocol.input.ClickablePoint;
import com.ruiyun.jvppeteer.protocol.runtime.RemoteObject;
import com.ruiyun.jvppeteer.transport.websocket.CDPSession;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ElementHandle represents an in-page DOM element. ElementHandles can be created with the page.$ method.
 */
public class ElementHandle extends JSHandle {

    private ExecutionContext context;

    private CDPSession client;

    private RemoteObject remoteObject;

    private Page page;

    @Override
    public Map<String, JSHandle> getProperties() throws JsonProcessingException {
        return super.getProperties();
    }

    private FrameManager frameManager;


    public ElementHandle(ExecutionContext context, CDPSession client, RemoteObject remoteObject, Page page, FrameManager frameManager) {
        super(context, client, remoteObject);
        this.client = client;
        this.remoteObject = remoteObject;
        this.page = page;
        this.frameManager = frameManager;
    }

    public ElementHandle asElement() {
        return this;
    }

    public Frame contentFrame() {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", this.remoteObject.getObjectId());
        JsonNode nodeInfo = this.client.send("DOM.describeNode", params, true);
        JsonNode frameId = nodeInfo.get("node").get("frameId");
        if (frameId == null || StringUtil.isEmpty(frameId.asText()))
            return null;
        return this.frameManager.frame(frameId.asText());
    }

    public void scrollIntoViewIfNeeded() throws JsonProcessingException {
        String pageFunction = "async (element, pageJavascriptEnabled) => {\n" +
                "            if (!element.isConnected)\n" +
                "                return 'Node is detached from document';\n" +
                "            if (element.nodeType !== Node.ELEMENT_NODE)\n" +
                "                return 'Node is not of type HTMLElement';\n" +
                "            // force-scroll if page's javascript is disabled.\n" +
                "            if (!pageJavascriptEnabled) {\n" +
                "                // Chrome still supports behavior: instant but it's not in the spec so TS shouts\n" +
                "                // We don't want to make this breaking change in Puppeteer yet so we'll ignore the line.\n" +
                "                // @ts-ignore\n" +
                "                element.scrollIntoView({ block: 'center', inline: 'center', behavior: 'instant' });\n" +
                "                return false;\n" +
                "            }\n" +
                "            const visibleRatio = await new Promise(resolve => {\n" +
                "                const observer = new IntersectionObserver(entries => {\n" +
                "                    resolve(entries[0].intersectionRatio);\n" +
                "                    observer.disconnect();\n" +
                "                });\n" +
                "                observer.observe(element);\n" +
                "            });\n" +
                "            if (visibleRatio !== 1.0) {\n" +
                "                // Chrome still supports behavior: instant but it's not in the spec so TS shouts\n" +
                "                // We don't want to make this breaking change in Puppeteer yet so we'll ignore the line.\n" +
                "                // @ts-ignore\n" +
                "                element.scrollIntoView({ block: 'center', inline: 'center', behavior: 'instant' });\n" +
                "            }\n" +
                "            return false;\n" +
                "        }";
        Object error = this.evaluate(pageFunction, PageEvaluateType.FUNCTION, this.page.getJavascriptEnabled());
        if (error != null)
            throw new RuntimeException(Constant.OBJECTMAPPER.writeValueAsString(error));
    }

    private ClickablePoint clickablePoint() {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", this.remoteObject.getObjectId());
        JsonNode result = this.client.send("DOM.getContentQuads", params, true);
        JsonNode layoutMetrics = this.client.send("Page.getLayoutMetrics", null, true);


        if (result == null || result.get("quads").size() == 0)
            throw new RuntimeException("Node is either not visible or not an HTMLElement");
        // Filter out quads that have too small area to click into.
        JsonNode layoutViewport = layoutMetrics.get("layoutViewport");
        JsonNode clientWidth = layoutViewport.get("clientWidth");
        JsonNode clientHeight = layoutMetrics.get("clientHeight");
        JsonNode quadsNode = result.get("quads");
        Iterator<JsonNode> elements = quadsNode.elements();
        List<List<ClickablePoint>> quads = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode quad = elements.next();
            List<ClickablePoint> clickOptions = this.fromProtocolQuad(quad);
            intersectQuadWithViewport(clickOptions, clientWidth.asInt(), clientHeight.asInt());
        }

        List<List<ClickablePoint>> collect = quads.stream().filter(quad -> computeQuadArea(quad) > 1).collect(Collectors.toList());
        if (collect.size() == 0)
            throw new RuntimeException("Node is either not visible or not an HTMLElement");
        // Return the middle point of the first quad.
        List<ClickablePoint> quad = collect.get(0);
        int x = 0;
        int y = 0;
        for (ClickablePoint point : quad) {
            x += point.getX();
            y += point.getY();
        }
        return new ClickablePoint(x / 4, y / 4);
    }

    private JsonNode getBoxModel() {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", this.remoteObject.getObjectId());
        return this.client.send("DOM.getBoxModel", params, true);
    }

    public String screenshot(ScreenshotOptions options) throws IOException {
        boolean needsViewportReset = false;
        Clip boundingBox = this.boundingBox();
        ValidateUtil.assertBoolean(boundingBox != null, "Node is either not visible or not an HTMLElement");
        Viewport viewport = this.page.viewport();
        if (viewport != null && (boundingBox.getWidth() > viewport.getWidth() || boundingBox.getHeight() > viewport.getHeight())) {
            Viewport newViewport = new Viewport();
            newViewport.setWidth(Math.max(viewport.getWidth(), Math.ceil(boundingBox.getWidth())));
            newViewport.setHeight(Math.max(viewport.getHeight(), Math.ceil(boundingBox.getHeight())));

            this.page.setViewport(newViewport);
            needsViewportReset = true;
        }
        this.scrollIntoViewIfNeeded();
        boundingBox = this.boundingBox();
        ValidateUtil.assertBoolean(boundingBox != null, "Node is either not visible or not an HTMLElement");
        ValidateUtil.assertBoolean(boundingBox.getWidth() != 0, "Node has 0 width.");
        ValidateUtil.assertBoolean(boundingBox.getHeight() != 0, "Node has 0 height.");
        JsonNode response = this.client.send("Page.getLayoutMetrics", null, true);
        double pageX = response.get("layoutViewport").get("pageX").asDouble();
        double pageY = response.get("layoutViewport").get("pageY").asDouble();
        Clip clip = boundingBox;
        clip.setX(clip.getX() + pageX);
        clip.setY(clip.getY() + pageY);

        options.setClip(clip);
        String imageData = this.page.screenshot(options);
        if (needsViewportReset)
            this.page.setViewport(viewport);
        return imageData;
    }

    public BoxModel boxModel() {
        JsonNode result = this.getBoxModel();
        if (result == null)
            return null;
        JsonNode model = result.get("model");
        List<ClickablePoint> content = this.fromProtocolQuad(model.get("content"));
        List<ClickablePoint> padding = this.fromProtocolQuad(model.get("padding"));
        List<ClickablePoint> border = this.fromProtocolQuad(model.get("border"));
        List<ClickablePoint> margin = this.fromProtocolQuad(model.get("margin"));
        int width = model.get("width").asInt();
        int height = model.get("height").asInt();
        return new BoxModel(content, padding, border, margin, width, height);
    }

    public int computeQuadArea(List<ClickablePoint> quad) {
        // Compute sum of all directed areas of adjacent triangles
        // https://en.wikipedia.org/wiki/Polygon#Simple_polygons
        int area = 0;
        for (int i = 0; i < quad.size(); ++i) {
            ClickablePoint p1 = quad.get(i);
            ClickablePoint p2 = quad.get((i + 1) % quad.size());
            area += (p1.getX() * p2.getY() - p2.getX() * p1.getY()) / 2;
        }
        return Math.abs(area);
    }

    private List<ClickablePoint> fromProtocolQuad(JsonNode quad) {
        List<ClickablePoint> result = new ArrayList<>();
        if (quad.isArray()) {
            result.add(new ClickablePoint(quad.get(0).asInt(), quad.get(1).asInt()));
            result.add(new ClickablePoint(quad.get(2).asInt(), quad.get(3).asInt()));
            result.add(new ClickablePoint(quad.get(4).asInt(), quad.get(5).asInt()));
            result.add(new ClickablePoint(quad.get(6).asInt(), quad.get(7).asInt()));
        } else {
            result.add(new ClickablePoint(quad.get("0").asInt(), quad.get("1").asInt()));
            result.add(new ClickablePoint(quad.get("2").asInt(), quad.get("3").asInt()));
            result.add(new ClickablePoint(quad.get(4).asInt(), quad.get("5").asInt()));
            result.add(new ClickablePoint(quad.get("6").asInt(), quad.get("7").asInt()));
        }
        return result;
    }

    public void intersectQuadWithViewport(List<ClickablePoint> quad, int width, int height) {
        for (ClickablePoint point : quad) {
            point.setX(Math.min(Math.max(point.getX(), 0), width));
            point.setY(Math.min(Math.max(point.getY(), 0), height));
        }
    }

    public ElementHandle $(String selector) throws JsonProcessingException {
        String defaultHandler = "(element, selector) => element.querySelector(selector)";
        QuerySelector queryHandlerAndSelector = QueryHandler.getQueryHandlerAndSelector(selector, defaultHandler);
        JSHandle handle = (JSHandle) this.evaluateHandle(queryHandlerAndSelector.getQueryHandler(), PageEvaluateType.FUNCTION, queryHandlerAndSelector.getUpdatedSelector());
        ElementHandle element = handle.asElement();
        if (element != null)
            return element;
        handle.dispose();
        return null;
    }

    public List<ElementHandle> $x(String expression) throws JsonProcessingException {
        String pageFunction = "(element, expression) => {\n" +
                "            const document = element.ownerDocument || element;\n" +
                "            const iterator = document.evaluate(expression, element, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE);\n" +
                "            const array = [];\n" +
                "            let item;\n" +
                "            while ((item = iterator.iterateNext()))\n" +
                "                array.push(item);\n" +
                "            return array;\n" +
                "        }";
        JSHandle arrayHandle = (JSHandle) this.evaluateHandle(pageFunction, PageEvaluateType.FUNCTION, expression);
        Map<String, JSHandle> properties = arrayHandle.getProperties();
        arrayHandle.dispose();
        List<ElementHandle> result = new ArrayList<>();
        for (JSHandle property : properties.values()) {
            ElementHandle elementHandle = property.asElement();
            if (elementHandle != null)
                result.add(elementHandle);
        }
        return result;
    }

    public ElementHandle $eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) throws JsonProcessingException {
        ElementHandle elementHandle = this.$(selector);
        if (elementHandle == null)
            throw new RuntimeException("failed to find element matching selector " + selector);
        ElementHandle result = (ElementHandle) elementHandle.evaluate(pageFunction, type, args);
        elementHandle.dispose();
        return result;
    }

    public Object $$eval(String selector, String pageFunction, PageEvaluateType type, Object[] args) throws JsonProcessingException {
        String defaultHandler = "(element, selector) => Array.from(element.querySelectorAll(selector))";
        QuerySelector queryHandlerAndSelector = QueryHandler.getQueryHandlerAndSelector(selector, defaultHandler);

        ElementHandle arrayHandle = (ElementHandle) this.evaluateHandle(queryHandlerAndSelector.getQueryHandler(), type, queryHandlerAndSelector.getUpdatedSelector());
        ElementHandle result = (ElementHandle) arrayHandle.evaluate(pageFunction, type, args);
        arrayHandle.dispose();
        return result;
    }

    public List<ElementHandle> $$(String selector) throws JsonProcessingException {
        String defaultHandler = "(element, selector) => element.querySelectorAll(selector)";
        QuerySelector queryHandlerAndSelector = QueryHandler.getQueryHandlerAndSelector(selector, defaultHandler);
        JSHandle arrayHandle = (JSHandle) this.evaluateHandle(queryHandlerAndSelector.getQueryHandler(), PageEvaluateType.FUNCTION, queryHandlerAndSelector.getUpdatedSelector());
        Map<String, JSHandle> properties = arrayHandle.getProperties();
        arrayHandle.dispose();
        List<ElementHandle> result = new ArrayList<>();
        for (JSHandle property : properties.values()) {
            ElementHandle elementHandle = property.asElement();
            if (elementHandle != null)
                result.add(elementHandle);
        }
        return result;
    }
    public boolean isIntersectingViewport() throws JsonProcessingException {
        String pageFunction ="async (element) => {\n" +
                "            const visibleRatio = await new Promise(resolve => {\n" +
                "                const observer = new IntersectionObserver(entries => {\n" +
                "                    resolve(entries[0].intersectionRatio);\n" +
                "                    observer.disconnect();\n" +
                "                });\n" +
                "                observer.observe(element);\n" +
                "            });\n" +
                "            return visibleRatio > 0;\n" +
                "        }";
        return  (Boolean) this.evaluate(pageFunction,PageEvaluateType.FUNCTION);
    }
    public void click(ClickOptions options) throws JsonProcessingException, InterruptedException {
        this.scrollIntoViewIfNeeded();
        ClickablePoint point = this.clickablePoint();
        this.page.getMouse().click(point.getX(), point.getY(), options);
    }

    public void focus() throws JsonProcessingException {
        this.evaluate("element => element.focus()", PageEvaluateType.FUNCTION);
    }

    public void hover() throws JsonProcessingException {
        this.scrollIntoViewIfNeeded();
        ClickablePoint clickablePoint = this.clickablePoint();
        this.page.getMouse().move(clickablePoint.getX(), clickablePoint.getX(), 0);
    }

    public List<String> select(List<String> values) throws JsonProcessingException {
        /* TODO(jacktfranklin@): once ExecutionContext is TypeScript, and
         * its evaluate function is properly typed with generics we can
         * return here and remove the typecasting
         */
        String pageFunction = "(element, values) => {\n" +
                "            if (element.nodeName.toLowerCase() !== 'select')\n" +
                "                throw new Error('Element is not a <select> element.');\n" +
                "            const options = Array.from(element.options);\n" +
                "            element.value = undefined;\n" +
                "            for (const option of options) {\n" +
                "                option.selected = values.includes(option.value);\n" +
                "                if (option.selected && !element.multiple)\n" +
                "                    break;\n" +
                "            }\n" +
                "            element.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                "            element.dispatchEvent(new Event('change', { bubbles: true }));\n" +
                "            return options.filter(option => option.selected).map(option => option.value);\n" +
                "        }";

        return (List<String>) this.evaluate(pageFunction, PageEvaluateType.FUNCTION, values);
    }

    public void tap() throws JsonProcessingException {
        this.scrollIntoViewIfNeeded();
        ClickablePoint point = this.clickablePoint();
        this.page.getTouchscreen().tap(point.getX(), point.getY());
    }

    public void type(String text, int delay) throws JsonProcessingException, InterruptedException {
        this.focus();
        this.page.getKeyboard().type(text, delay);
    }

    public void press(String key, int delay, String text) throws JsonProcessingException, InterruptedException {
        this.focus();
        this.page.getKeyboard().press(key, delay, text);
    }

    public Clip boundingBox() {
        JsonNode result = this.getBoxModel();
        if (result == null)
            return null;
        JsonNode quad = result.get("model").get("border");
        int x = Math.min(Math.min(Math.min(quad.get(0).asInt(), quad.get(2).asInt()), quad.get(4).asInt()), quad.get(6).asInt());
        int y = Math.min(Math.min(Math.min(quad.get(1).asInt(), quad.get(3).asInt()), quad.get(5).asInt()), quad.get(7).asInt());
        int width = Math.max(Math.max(Math.max(quad.get(0).asInt(), quad.get(2).asInt()), quad.get(4).asInt()), quad.get(6).asInt()) - x;
        int height = Math.max(Math.max(Math.max(quad.get(1).asInt(), quad.get(3).asInt()), quad.get(5).asInt()), quad.get(7).asInt()) - y;

        return new Clip(x, y, width, height);
    }

    public void uploadFile(List<String> filePaths) throws JsonProcessingException {
        boolean isMultiple = (Boolean) this.evaluate("(element) => element.multiple", PageEvaluateType.FUNCTION);
        ValidateUtil.assertBoolean(filePaths.size() <= 1 || isMultiple, "Multiple file uploads only work with <input type=file multiple>");
        List<String> files = filePaths.stream().map(filePath -> {
            Path absolutePath = Paths.get(filePath).toAbsolutePath();
            boolean readable = Files.isReadable(absolutePath);
            if (!readable) {
                throw new AccessControlException(filePath + "is not readable");
            }
            return absolutePath.toString();
        }).collect(Collectors.toList());
        String objectId = this.remoteObject.getObjectId();
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);
        JsonNode node = this.client.send("DOM.describeNode", params, true);
        int backendNodeId = node.get("backendNodeId").asInt();
        // The zero-length array is a special case, it seems that DOM.setFileInputFiles does
        // not actually update the files in that case, so the solution is to eval the element
        // value to a new FileList directly.
        if (files.size() == 0) {
            String pageFunction = "(element) => {\n" +
                    "                    element.files = new DataTransfer().files;\n" +
                    "            // Dispatch events for this case because it should behave akin to a user action.\n" +
                    "            element.dispatchEvent(new Event('input', { bubbles: true }));\n" +
                    "            element.dispatchEvent(new Event('change', { bubbles: true }));\n" +
                    "            }";
            this.evaluate(pageFunction, PageEvaluateType.FUNCTION);
        } else {
            params.clear();
            params.put("objectId", objectId);
            params.put("files", files);
            params.put("backendNodeId", backendNodeId);
            this.client.send("DOM.setFileInputFiles", params, true);
        }
    }

    public RemoteObject getRemoteObject() {
        return null;
    }


}

