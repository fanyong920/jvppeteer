package com.ruiyun.jvppeteer.cdp.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.api.core.CDPSession;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.cdp.entities.AutofillData;
import com.ruiyun.jvppeteer.cdp.entities.RemoteObject;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ElementHandle 表示页内 DOM 元素。
 * <p>
 * ElementHandles 可以使用 Page.$() 方法创建。
 * <p>
 * ElementHandle 会阻止 DOM 元素被垃圾回收，除非句柄是 disposed。当其原始框架被导航时，ElementHandles 会被自动处理。
 * <p>b
 * ElementHandle 实例可以用作 Page.$eval() 和 Page.evaluate() 方法中的参数。
 * <p>
 * 此类的构造函数被标记为内部构造函数。第三方代码不应直接调用构造函数或创建扩展 ElementHandle 类的子类。
 */
public class CdpElementHandle extends ElementHandle {

    private static final Set<String> NON_ELEMENT_NODE_ROLES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("StaticText", "InlineTextBox")));

    CdpElementHandle(IsolatedWorld world, RemoteObject remoteObject) {
        super(new CdpJSHandle(world, remoteObject));
    }

    public IsolatedWorld realm() {
        return this.handle.realm().toIsolatedWorld();
    }

    public CDPSession client() {
        return ((CdpJSHandle) (this.handle)).client();
    }

    public FrameManager frameManager() {
        return this.frame().frameManager();
    }

    @Override
    public CdpFrame frame() {
        return this.realm().toIsolatedWorld().getFrame();
    }

    @Override
    public CdpFrame contentFrame() {
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.id());
        JsonNode nodeInfo = this.client().send("DOM.describeNode", params);
        JsonNode frameId = nodeInfo.get("node").get("frameId");
        if (frameId == null || StringUtil.isEmpty(frameId.asText()))
            return null;
        return this.frameManager().frame(frameId.asText());
    }


    @Override
    public void scrollIntoView() throws JsonProcessingException, EvaluateException {
        this.assertConnectedElement();
        try {
            Map<String, Object> params = ParamsFactory.create();
            params.put("objectId", this.id());
            this.client().send("DOM.scrollIntoViewIfNeeded", params);
        } catch (Exception e) {
            LOGGER.error("jvppeteer error", e);
            super.scrollIntoView();
        }
    }

    @Override
    public void uploadFile(List<String> filePaths) throws JsonProcessingException, EvaluateException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        boolean isMultiple = (Boolean) wrapThis.evaluate("(element) => element.multiple");
        ValidateUtil.assertArg(filePaths.size() <= 1 || isMultiple, "Multiple file uploads only work with <input type=file multiple>");
        List<String> files = filePaths.stream().map(filePath -> {
            Path absolutePath = Paths.get(filePath).toAbsolutePath();
            boolean readable = Files.isReadable(absolutePath);
            if (!readable) {
                throw new AccessControlException(filePath + "is not readable");
            }
            return absolutePath.toString();
        }).collect(Collectors.toList());
        // The zero-length array is a special case, it seems that DOM.setFileInputFiles does
        // not actually update the files in that case, so the solution is to eval the element
        // value to a new FileList directly.
        if (files.isEmpty()) {
            String pptrFunction = "element => {\n" +
                    "        element.files = new DataTransfer().files;\n" +
                    "\n" +
                    "        // Dispatch events for this case because it should behave akin to a user action.\n" +
                    "        element.dispatchEvent(\n" +
                    "          new Event('input', {bubbles: true, composed: true})\n" +
                    "        );\n" +
                    "        element.dispatchEvent(new Event('change', {bubbles: true}));\n" +
                    "      }";
            wrapThis.evaluate(pptrFunction);
        } else {
            String objectId = wrapThis.id();
            Map<String, Object> params = ParamsFactory.create();
            params.put("objectId", objectId);
            JsonNode node = ((CdpElementHandle) wrapThis).client().send("DOM.describeNode", params);
            int backendNodeId = node.get("node").get("backendNodeId").asInt();
            params.clear();
            params.put("objectId", objectId);
            params.put("files", files);
            params.put("backendNodeId", backendNodeId);
            ((CdpElementHandle) wrapThis).client().send("DOM.setFileInputFiles", params);
        }
    }

    @Override
    public void autofill(AutofillData data) {
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.id());
        JsonNode response = this.client().send("DOM.describeNode", params);
        int fieldId = response.get("node").get("backendNodeId").asInt();
        String frameId = this.frame().id();
        params.clear();
        params.put("fieldId", fieldId);
        params.put("frameId", frameId);
        params.put("card", data.getCreditCard());
        this.client().send("Autofill.trigger", params);
    }

    @Override
    public List<ElementHandle> queryAXTree(String name, String role) throws JsonProcessingException {
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.id());
        params.put("accessibleName", name);
        params.put("role", role);
        JsonNode response = this.client().send("Accessibility.queryAXTree", params);
        JsonNode nodes = response.get("nodes");
        Iterator<JsonNode> elements = nodes.elements();
        List<ElementHandle> result = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode node = elements.next();
            if (node.hasNonNull("ignored") && node.get("ignored").asBoolean() || !node.hasNonNull("role") || NON_ELEMENT_NODE_ROLES.contains(node.get("role").get("value").asText())) {
                continue;
            }
            result.add(this.realm().adoptBackendNode(node.get("backendDOMNodeId").asInt()).asElement());
        }
        return result;
    }

}

