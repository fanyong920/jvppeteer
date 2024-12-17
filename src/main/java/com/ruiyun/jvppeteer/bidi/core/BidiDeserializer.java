package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.ruiyun.jvppeteer.bidi.entities.RemoteValue;
import com.ruiyun.jvppeteer.common.Constant;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BidiDeserializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(BidiDeserializer.class);

    public static Object deserialize(RemoteValue result) {

        if (result == null) {
            LOGGER.error("Service did not produce a result.");
            return null;
        }

        switch (result.getType()) {
            case "array":
                List<Object> array = new ArrayList<>();
                Iterator<JsonNode> arrayElements = result.getValue().elements();
                while (arrayElements.hasNext()) {
                    JsonNode element = arrayElements.next();
                    array.add(deserialize(Constant.OBJECTMAPPER.convertValue(element, RemoteValue.class)));
                }
                return array;
            case "set":
                List<Object> set = new ArrayList<>();
                Iterator<JsonNode> setElements = result.getValue().elements();
                while (setElements.hasNext()) {
                    JsonNode element = setElements.next();
                    set.add(deserialize(Constant.OBJECTMAPPER.convertValue(element, RemoteValue.class)));
                }
                return set;
            case "object":
                Map<Object, Object> objectResult = new HashMap<>();
                JsonNode resultValue = result.getValue();
                if (Objects.nonNull(resultValue)) {
                    Iterator<JsonNode> objectElements = resultValue.elements();
                    while (objectElements.hasNext()) {
                        JsonNode element = objectElements.next();
                        JsonNode key = element.get(0);
                        JsonNode value = element.get(1);
                        if (key.isTextual()) {
                            objectResult.put(key.asText(), deserialize(Constant.OBJECTMAPPER.convertValue(value, RemoteValue.class)));
                        } else {
                            objectResult.put(deserialize(Constant.OBJECTMAPPER.convertValue(key, RemoteValue.class)), deserialize(Constant.OBJECTMAPPER.convertValue(value, RemoteValue.class)));
                        }

                    }
                }
                return Constant.OBJECTMAPPER.convertValue(objectResult, Object.class);
            case "map":
                Map<Object, Object> mapResult = new HashMap<>();
                Iterator<JsonNode> mapElements = result.getValue().elements();
                while (mapElements.hasNext()) {
                    JsonNode element = mapElements.next();
                    JsonNode key = element.get(0);
                    JsonNode value = element.get(1);
                    if (key.isTextual()) {
                        mapResult.put(key.asText(), deserialize(Constant.OBJECTMAPPER.convertValue(value, RemoteValue.class)));
                    } else {
                        mapResult.put(deserialize(Constant.OBJECTMAPPER.convertValue(key, RemoteValue.class)), deserialize(Constant.OBJECTMAPPER.convertValue(value, RemoteValue.class)));
                    }

                }
                return mapResult;
            case "promise":
                return new HashMap<>(); // Placeholder for promise
            case "regexp":
                return Pattern.compile(result.getValue().get("pattern").asText(), convertJsFlagsToJavaFlags(result.getValue().get("flags").asText()));
            case "date":
                return new Date(result.getValue().asLong());
            case "undefined":
                return null; // Java does not have undefined, so we use null
            case "null":
                return null;
            case "number":
                return deserializeNumber(result.getValue());
            case "bigint":
                return new BigInteger(result.getValue().asText());
            case "boolean":
                return result.getValue().asBoolean();
            case "string":
                return result.getValue().asText();
        }

        LOGGER.error("Deserialization of type {} not supported.", result.getType());
        return null;
    }

    private static Object deserializeNumber(JsonNode value) {
        switch (value.asText()) {
            case "-0":
                return -0;
            case "NaN":
                return Double.NaN;
            case "Infinity":
                return Double.POSITIVE_INFINITY;
            case "-Infinity":
                return Double.NEGATIVE_INFINITY;
            default:
                return Double.valueOf(value.asText());
        }
    }

    public static int convertJsFlagsToJavaFlags(String jsFlags) {
        int javaFlags = 0;

        if (jsFlags.contains("i")) {
            javaFlags |= Pattern.CASE_INSENSITIVE;
        }
        if (jsFlags.contains("m")) {
            javaFlags |= Pattern.MULTILINE;
        }
        if (jsFlags.contains("s")) {
            javaFlags |= Pattern.DOTALL;
        }
        if (jsFlags.contains("u")) {
            javaFlags |= Pattern.UNICODE_CASE;
        }
        // Note: 'g' and 'y' are not directly supported in Java, so they are ignored here.

        return javaFlags;
    }
}
