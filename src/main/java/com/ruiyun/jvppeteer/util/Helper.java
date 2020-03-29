package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.Constant;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Helper implements Constant {
    public static String createProtocolError(JsonNode node) {
        JsonNode methodNode = node.get(RECV_MESSAGE_METHOD_PROPERTY);
        JsonNode errNode = node.get(RECV_MESSAGE_ERROR_PROPERTY);
        JsonNode errorMsg = errNode.get(RECV_MESSAGE_ERROR_MESSAGE_PROPERTY);
        String message = "Protocol error "+methodNode.asText()+": "+errorMsg;
        JsonNode dataNode = errNode.get(RECV_MESSAGE_ERROR_DATA_PROPERTY);
        if(dataNode != null) {
            message += " "+dataNode.asText();
        }
        return message;
    }

}
