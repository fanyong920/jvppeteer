package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.api.core.ElementHandle;
import com.ruiyun.jvppeteer.api.core.Frame;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.cdp.entities.AutofillData;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.ParamsFactory;
import com.ruiyun.jvppeteer.exception.EvaluateException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessControlException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BidiElementHandle extends ElementHandle {
    private Integer backendNodeId;

    public BidiElementHandle(RemoteValue value, BidiFrameRealm realm) {
        super(BidiJSHandle.from(value, realm));
    }

    @Override
    public BidiFrameRealm realm() {
        return (BidiFrameRealm) this.handle.realm();
    }

    @Override
    public BidiFrame frame() {
        return this.realm().frame();
    }

    public RemoteValue remoteValue() {
        return ((BidiJSHandle) this.handle).remoteValue();
    }

    @Override
    public void autofill(AutofillData data) {
        BidiCdpSession client = this.frame().client();
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.handle.id());
        JsonNode nodeInfo = client.send("DOM.describeNode", params);
        JsonNode fieldId = nodeInfo.at("/node/backendNodeId");
        String frameId = this.frame().id();
        Map<String, Object> params2 = ParamsFactory.create();
        params2.put("frameId", frameId);
        params2.put("fieldId", fieldId.asText());
        params2.put("card", data.getCreditCard());
        client.send("Autofill.trigger", params2);
    }

    @Override
    public int backendNodeId() {
        if (!this.frame().page().browser().cdpSupported()) {
            throw new UnsupportedOperationException();
        }
        if (Objects.nonNull(this.backendNodeId)) {
            return this.backendNodeId;
        }
        Map<String, Object> params = ParamsFactory.create();
        params.put("objectId", this.id());
        JsonNode nodeInfo = this.frame().client().send("DOM.describeNode", params);
        this.backendNodeId = nodeInfo.at("/node/backendNodeId").asInt();
        return this.backendNodeId;
    }

    @Override
    public Frame contentFrame() throws JsonProcessingException {
        ElementHandle wrapThis = this.adoptIsolatedHandle();
        BidiJSHandle jsHandle = (BidiJSHandle) wrapThis.evaluateHandle("element => {\n" +
                "  if (\n" +
                "    element instanceof HTMLIFrameElement ||\n" +
                "    element instanceof HTMLFrameElement\n" +
                "  ) {\n" +
                "    return element.contentWindow;\n" +
                "  }\n" +
                "  return;\n" +
                "}");
        try {
            RemoteValue remoteValue = jsHandle.remoteValue();
            if (Objects.equals("window", remoteValue.getType())) {
                JsonNode value = remoteValue.getValue();
                return wrapThis.frame().page().frames().stream().filter(frame -> Objects.equals(frame.id(), value.get("context").asText())).findFirst().orElse(null);
            }
        } finally {
            jsHandle.disposed();
        }
        return null;
    }

    @Override
    public void uploadFile(List<String> filePaths) throws EvaluateException, JsonProcessingException {
        List<String> files = filePaths.stream().map(filePath -> {
            Path absolutePath = Paths.get(filePath).toAbsolutePath();
            boolean readable = Files.isReadable(absolutePath);
            if (!readable) {
                throw new AccessControlException(filePath + "is not readable");
            }
            return absolutePath.toString();
        }).collect(Collectors.toList());
        this.frame().setFiles(this, files);
    }

    @Override
    public List<ElementHandle> queryAXTree(String name, String role) throws JsonProcessingException {
        ObjectNode locator = Constant.OBJECTMAPPER.createObjectNode();
        locator.put("type", "accessibility");
        ObjectNode value = Constant.OBJECTMAPPER.createObjectNode();
        value.put("role", role);
        value.put("name", name);
        locator.set("value", value);
        List<RemoteValue> results = this.frame().locateNodes(this, locator);
        return results.stream().map(node -> BidiElementHandle.from(node, this.realm())).collect(Collectors.toList());
    }

    public static BidiElementHandle from(RemoteValue node, BidiFrameRealm realm) {
        return new BidiElementHandle(node, realm);
    }
}
