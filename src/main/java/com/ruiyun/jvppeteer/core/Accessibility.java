package com.ruiyun.jvppeteer.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.entities.SnapshotOptions;
import com.ruiyun.jvppeteer.entities.SerializedAXNode;
import com.ruiyun.jvppeteer.util.StringUtil;
import com.ruiyun.jvppeteer.util.ValidateUtil;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Accessibility {
    private final Realm realm;

    public Accessibility(Realm realm) {
        this.realm = realm;
    }

    /**
     * 捕获可访问性树的当前状态。返回的对象表示页面的根可访问节点。
     * <p>
     * 注意 Chrome 辅助功能树包含大多数平台和大多数屏幕阅读器都未使用的节点。Puppeteer 也会丢弃它们，以便更容易处理树，除非 interestingOnly 设置为 false。
     *
     * @throws JsonProcessingException 异常
     * @throws IllegalAccessException 异常
     * @throws IntrospectionException 异常
     * @throws InvocationTargetException 异常
     * @return 代表快照的 AXNode 对象。
     */
    public SerializedAXNode snapshot() throws JsonProcessingException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        return this.snapshot(new SnapshotOptions());
    }

    public SerializedAXNode snapshot(SnapshotOptions options) throws JsonProcessingException, IllegalAccessException, IntrospectionException, InvocationTargetException {
        JsonNode nodes = this.realm.environment().client().send("Accessibility.getFullAXTree").get("nodes");
        String backendNodeId = null;
        if (options.getRoot() != null) {
            Map<String, Object> params = new HashMap<>();
            params.put("objectId", options.getRoot().id());
            JsonNode node = this.realm.environment().client().send("DOM.describeNode", params);
            backendNodeId = node.get("backendNodeId").asText();
        }
        Iterator<JsonNode> elements = nodes.elements();
        List<com.ruiyun.jvppeteer.entities.AXNode> payloads = new ArrayList<>();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            payloads.add(Constant.OBJECTMAPPER.treeToValue(next, com.ruiyun.jvppeteer.entities.AXNode.class));
        }
        AXNode defaultRoot = AXNode.createTree(realm, payloads);
        AXNode needle = defaultRoot;
        if (StringUtil.isNotEmpty(backendNodeId)) {
            String finalBackendNodeId = backendNodeId;
            needle = defaultRoot.find(node -> finalBackendNodeId.equals(node.getPayload().getBackendDOMNodeId() + ""));
            if (needle == null)
                return null;
        }
        if (!options.getInterestingOnly())
            return this.serializeTree(needle, null).get(0);
        Set<AXNode> interestingNodes = new HashSet<>();
        this.collectInterestingNodes(interestingNodes, defaultRoot, false);
        if (!interestingNodes.contains(needle))
            return null;
        return this.serializeTree(needle, interestingNodes).get(0);
    }

    private void collectInterestingNodes(Set<AXNode> collection, AXNode node, boolean insideControl) {
        if (node.isInteresting(insideControl))
            collection.add(node);
        if (node.isLeafNode())
            return;
        insideControl = insideControl || node.isControl();
        for (AXNode child : node.getChildren())
            this.collectInterestingNodes(collection, child, insideControl);
    }

    private List<SerializedAXNode> serializeTree(AXNode node, Set<AXNode> interestingNodes) throws IllegalAccessException, IntrospectionException, InvocationTargetException, JsonProcessingException {
        List<SerializedAXNode> children = new ArrayList<>();
        if (ValidateUtil.isNotEmpty(node.getChildren())) {
            for (AXNode child : node.getChildren())
                children.addAll(this.serializeTree(child, interestingNodes));
        }
        if (ValidateUtil.isNotEmpty(interestingNodes) && !interestingNodes.contains(node))
            return children;
        SerializedAXNode serializedNode = node.serialize();
        if (ValidateUtil.isNotEmpty(children))
            serializedNode.setChildren(children);
        List<SerializedAXNode> result = new ArrayList<>();
        result.add(serializedNode);
        return result;
    }
}
