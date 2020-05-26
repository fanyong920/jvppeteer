package com.ruiyun.example.sometest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.core.Constant;
import com.ruiyun.jvppeteer.core.page.PaperFormats;
import com.ruiyun.jvppeteer.protocol.log.DialogType;
import org.junit.Test;

import java.text.MessageFormat;
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
        String s = "adasd\nasdadad\nds\n".replaceAll("\n", "");
        System.out.println(s);
        System.out.println("adasd\nasdadad\nds\n");
        Map<String,String> map = new HashMap<>();
        map.put("\u0000","sadad");
        String s1 = map.get("\u0000");
        System.out.println(s1);
        boolean isXPath = true;
        boolean waitForHidden = true;
        String selectorOrXPath = "65466";

        String title = (isXPath ? "XPath" : "selector") +" "+ "\""+selectorOrXPath+"\""+ (waitForHidden ? " to be hidden":"");
        System.out.println(title);

        String fun = "saedad";
        List<String> argsList = new ArrayList<>();
        argsList.add("1231");
        argsList.add("56");
        argsList.add("8765");
        System.out.println( MessageFormat.format("({0})({1})",fun,String.join(",",argsList)));

        DialogType alert = DialogType.valueOf("Alert");
        String s2 = DialogType.Alert.toString();
        System.out.println("s2;"+s2);
        System.out.println(alert.getType());

        ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
        objectNode.put("12","3245");
        String s3 = Constant.OBJECTMAPPER.writeValueAsString(objectNode);
        System.out.println(s3);
        String aaa= "asas";
        System.out.println(aaa);

        PaperFormats letter = PaperFormats.valueOf("456");
        System.out.println(letter);
    }
}
