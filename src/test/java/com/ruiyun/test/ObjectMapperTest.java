package com.ruiyun.test;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

public class ObjectMapperTest {

    @Test
    public void test1() throws IOException {
        String test = "{\"error\":{\"code\":-32000,\"message\":\"Failed to find browser context with id 5959\"},\"id\":2}";
        ObjectMapper OBJECTMAPPER = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonNode jsonNode = OBJECTMAPPER.readTree(test);
        JsonNode params = jsonNode.get("id");
        System.out.println(params.toString());
    }
}
