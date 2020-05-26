package com.ruiyun.jvppeteer.core.page;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.protocol.accessbility.SerializedAXNode;
import com.ruiyun.jvppeteer.transport.CDPSession;
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

    private CDPSession client;

    public Accessibility(CDPSession client) {
        this.client = client;
    }

    public SerializedAXNode snapshot(boolean interestingOnly , ElementHandle root) throws JsonProcessingException, IllegalAccessException, IntrospectionException, InvocationTargetException {

        JsonNode nodes =  this.client.send("Accessibility.getFullAXTree",null,false);
        String backendNodeId = null;
        if (root != null) {
            Map<String,Object> params = new HashMap<>();
            params.put("objectId",root.getRemoteObject().getObjectId());
      JsonNode node = this.client.send("DOM.describeNode",params,true);
            backendNodeId = node.get("backendNodeId").asText();
        }
        Iterator<JsonNode> elements = nodes.elements();
        List<com.ruiyun.jvppeteer.protocol.accessbility.AXNode> payloads = new ArrayList<>();
        while(elements.hasNext()){
            payloads.add(Constant.OBJECTMAPPER.treeToValue(elements.next(),com.ruiyun.jvppeteer.protocol.accessbility.AXNode.class));
        }
        AXNode defaultRoot = AXNode.createTree(payloads);
        AXNode needle = defaultRoot;
        if (StringUtil.isNotEmpty(backendNodeId)){
            String finalBackendNodeId = backendNodeId;
            needle = defaultRoot.find(node -> finalBackendNodeId.equals(node.getPayload().getBackendDOMNodeId()+""));
            if (needle == null)
                return null;
        }
        if (!interestingOnly)
            return serializeTree(needle,null).get(0);

    Set<AXNode> interestingNodes = new HashSet<>();
        collectInterestingNodes(interestingNodes, defaultRoot, false);
        if (!interestingNodes.contains(needle))
            return null;
        return serializeTree(needle, interestingNodes).get(0);
    }

    private void collectInterestingNodes(Set<AXNode> collection, AXNode node, boolean insideControl) {
        if (node.isInteresting(insideControl))
            collection.add(node);
        if (node.isLeafNode())
            return;
        insideControl = insideControl || node.isControl();
        for (AXNode child :
                node.getChildren())
        collectInterestingNodes(collection, child, insideControl);
    }

    public List<SerializedAXNode> serializeTree(AXNode node, Set<AXNode> whitelistedNodes) throws IllegalAccessException, IntrospectionException, InvocationTargetException {
        List<SerializedAXNode> children  = new ArrayList<>();
        for (AXNode child : node.getChildren())
        children.addAll(serializeTree(child, whitelistedNodes));

        if (ValidateUtil.isNotEmpty(whitelistedNodes) && !whitelistedNodes.contains(node))
            return children;

        SerializedAXNode serializedNode = node.serialize();
        if (ValidateUtil.isNotEmpty(children))
            serializedNode.setChildren(children);
        List<SerializedAXNode> result = new ArrayList<>();
        result.add(serializedNode);
        return result;
    }
}
