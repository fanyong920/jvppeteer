package com.ruiyun.test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.io.IOException;
import java.util.List;

public class ClassTest {
    public static void main(String[] args) throws IOException {
        JsonNode jsonNode = new JsonNode() {
            @Override
            public <T extends JsonNode> T deepCopy() {
                return null;
            }

            @Override
            public JsonNode get(int i) {
                return null;
            }

            @Override
            public JsonNode path(String s) {
                return null;
            }

            @Override
            public JsonNode path(int i) {
                return null;
            }

            @Override
            protected JsonNode _at(JsonPointer jsonPointer) {
                return null;
            }

            @Override
            public JsonNodeType getNodeType() {
                return null;
            }

            @Override
            public String asText() {
                return null;
            }

            @Override
            public JsonNode findValue(String s) {
                return null;
            }

            @Override
            public JsonNode findPath(String s) {
                return null;
            }

            @Override
            public JsonNode findParent(String s) {
                return null;
            }

            @Override
            public List<JsonNode> findValues(String s, List<JsonNode> list) {
                return null;
            }

            @Override
            public List<String> findValuesAsText(String s, List<String> list) {
                return null;
            }

            @Override
            public List<JsonNode> findParents(String s, List<JsonNode> list) {
                return null;
            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public boolean equals(Object o) {
                return false;
            }

            @Override
            public JsonToken asToken() {
                return null;
            }

            @Override
            public JsonParser.NumberType numberType() {
                return null;
            }

            @Override
            public JsonParser traverse() {
                return null;
            }

            @Override
            public JsonParser traverse(ObjectCodec objectCodec) {
                return null;
            }

            @Override
            public void serialize(JsonGenerator jsonGenerator, SerializerProvider serializerProvider) {

            }

            @Override
            public void serializeWithType(JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) {

            }
        };

        String simpleName = jsonNode.getClass().getSimpleName();
        String name = jsonNode.getClass().getName();
        String canonicalName = jsonNode.getClass().getCanonicalName();
        System.out.println("simpleName="+simpleName);
        System.out.println("name="+name);
        System.out.println("canonicalName="+canonicalName);
        System.out.println(JsonNode.class.isAssignableFrom(jsonNode.getClass()));
        readJsonObject(jsonNode.getClass(),jsonNode);
    }

    private static <T> T readJsonObject(Class<T> clazz, JsonNode jsonNode) {
        if (jsonNode == null) {
            throw new IllegalArgumentException(
                    "Failed converting null response to clazz " + clazz.getName());
        }
        if(JsonNode.class.isAssignableFrom(clazz)){
            System.out.println("asas");
            return (T)jsonNode;
        }
        return null;
    }
}
