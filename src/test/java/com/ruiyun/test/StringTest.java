package com.ruiyun.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruiyun.jvppeteer.Constant;
import org.junit.Test;

import java.util.*;

public class StringTest {

    @Test
    public void test1() throws JsonProcessingException {
//        String test = "ws://localhost:364545/browser/asdsadadsdfad-sdad-sada-dasd";
////        test = test.replace("ws://localhost:","");
////        int end = test.indexOf("/");
////        System.out.println(Integer.parseInt(test.substring(0,end)));
////
////        Properties properties = System.getProperties();
////        Set<Object> objects = properties.keySet();
////        for (Object object : objects) {
////            Object o = properties.get(object);
////            System.out.println(object+":"+o);
////        }

        Map<String,Object> params = new HashMap<>();
        params.put("handleAuthRequests",true);
        List<String> patterns = new ArrayList<>();
        patterns.add("{urlPattern: \"*\"}");
        params.put("patterns",patterns);
        System.out.println(Constant.OBJECTMAPPER.writeValueAsString(params));
    }
}
