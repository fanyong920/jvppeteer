package com.ruiyun.example.sometest;

import com.fasterxml.jackson.core.JsonProcessingException;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import org.junit.Test;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTest {

    @Test
    public void test1() throws JsonProcessingException, ScriptException, NoSuchMethodException {
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

//        Map<String,Object> params = new HashMap<>();
//        params.put("handleAuthRequests",true);
//        List<String> patterns = new ArrayList<>();
//        patterns.add("{urlPattern: \"*\"}");
//        params.put("patterns",patterns);
//        System.out.println(Constant.OBJECTMAPPER.writeValueAsString(params));
//        String s = "adasd\nasdadad\nds\n".replaceAll("\n", "");
//        System.out.println(s);
//        System.out.println("adasd\nasdadad\nds\n");
//        Map<String,String> map = new HashMap<>();
//        map.put("\u0000","sadad");
//        String s1 = map.get("\u0000");
//        System.out.println(s1);
//        boolean isXPath = true;
//        boolean waitForHidden = true;
//        String selectorOrXPath = "65466";
//
//        String title = (isXPath ? "XPath" : "selector") +" "+ "\""+selectorOrXPath+"\""+ (waitForHidden ? " to be hidden":"");
//        System.out.println(title);
//
//        String fun = "saedad";
//        List<String> argsList = new ArrayList<>();
//        argsList.add("1231");
//        argsList.add("56");
//        argsList.add("8765");
//        System.out.println( MessageFormat.format("({0})({1})",fun,String.join(",",argsList)));
//
//        DialogType alert = DialogType.valueOf("Alert");
//        String s2 = DialogType.Alert.toString();
//        System.out.println("s2;"+s2);
//        System.out.println(alert.getType());
//
//        ObjectNode objectNode = Constant.OBJECTMAPPER.createObjectNode();
//        objectNode.put("12","3245");
//        String s3 = Constant.OBJECTMAPPER.writeValueAsString(objectNode);
//        System.out.println(s3);
//        String aaa= "asas";
//        System.out.println(aaa);
//
//        PaperFormats letter = PaperFormats.valueOf("456");
//        System.out.println(letter);

        String content = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <title>chromium-browser-snapshots Mirror</title>\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <!-- Bootstrap -->\n" +
                "    <link href=\"https://cdn.staticfile.org/twitter-bootstrap/3.3.7/css/bootstrap.min.css\" rel=\"stylesheet\" media=\"screen\">\n" +
                "    <style>\n" +
                "      #fork{position:fixed;top:0;right:0;_position:absolute;z-index: 10000;}\n" +
                "      .bottom{margin: 20px auto; width: 100%; text-align: center;}\n" +
                "      .container{width: 1080px; margin: 50px auto;}\n" +
                "    </style>\n" +
                "  <head>\n" +
                "  <body>\n" +
                "    <a href=\"https://github.com/cnpm/cnpmjs.org\" id=\"fork\" target=\"_blank\">\n" +
                "        <img alt=\"Fork me on GitHub\" src=\"//s3.amazonaws.com/github/ribbons/forkme_right_darkblue_121621.png\">\n" +
                "    </a>\n" +
                "    <div class=\"container\">\n" +
                "      <h1>Mirror index of <a target=\"_blank\" href=\"https://github.com/GoogleChrome/puppeteer/Win_x64/\">https://github.com/GoogleChrome/puppeteer/Win_x64/</a></h1>\n" +
                "<hr>\n" +
                "<pre><a href=\"../\">../</a>\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/494755/\">494755/</a>                                           2017-08-16T15:24:41.336Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/496140/\">496140/</a>                                           2017-08-26T02:43:27.787Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/497674/\">497674/</a>                                           2017-09-02T04:04:52.227Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/499413/\">499413/</a>                                           2017-09-21T05:47:11.714Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/508693/\">508693/</a>                                           2017-10-14T16:56:25.974Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/515411/\">515411/</a>                                           2017-11-11T02:24:56.507Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/524617/\">524617/</a>                                           2017-12-28T22:31:49.054Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/526987/\">526987/</a>                                           2018-01-04T18:57:17.126Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/533271/\">533271/</a>                                           2018-02-01T00:04:51.353Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/536395/\">536395/</a>                                           2018-02-14T01:13:54.153Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/538022/\">538022/</a>                                           2018-02-22T02:25:00.858Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/543305/\">543305/</a>                                           2018-03-16T01:21:25.385Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/546920/\">546920/</a>                                           2018-03-29T21:16:54.820Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/548153/\">548153/</a>                                           2018-04-04T20:58:52.816Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/548921/\">548921/</a>                                           2018-04-06T23:14:26.746Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/549031/\">549031/</a>                                           2018-04-07T08:02:57.104Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/551292/\">551292/</a>                                           2018-04-17T18:50:44.515Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/553380/\">553380/</a>                                           2018-04-25T07:47:41.977Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/554716/\">554716/</a>                                           2018-05-01T00:41:36.034Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/555668/\">555668/</a>                                           2018-05-03T23:05:56.508Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/557152/\">557152/</a>                                           2018-05-09T17:13:56.138Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/563942/\">563942/</a>                                           2018-06-02T16:59:34.058Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/564778/\">564778/</a>                                           2018-06-06T14:54:13.757Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/567388/\">567388/</a>                                           2018-06-14T22:57:17.731Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/568432/\">568432/</a>                                           2018-06-19T20:48:35.211Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/571040/\">571040/</a>                                           2018-06-28T20:25:16.368Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/571375/\">571375/</a>                                           2018-06-29T17:03:26.943Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/574897/\">574897/</a>                                           2018-07-23T21:30:53.228Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/575458/\">575458/</a>                                           2018-07-24T18:21:00.323Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/579032/\">579032/</a>                                           2018-07-31T02:11:31.586Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/583214/\">583214/</a>                                           2018-08-16T01:57:18.749Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/587164/\">587164/</a>                                           2018-08-30T00:05:42.894Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/588429/\">588429/</a>                                           2018-09-04T09:08:12.712Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/590951/\">590951/</a>                                           2018-09-13T18:18:04.730Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/591618/\">591618/</a>                                           2018-09-17T22:25:19.168Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/594312/\">594312/</a>                                           2018-09-26T16:21:56.600Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/599821/\">599821/</a>                                           2018-10-16T18:28:52.342Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/604907/\">604907/</a>                                           2018-11-03T00:19:10.531Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/606647/\">606647/</a>                                           2018-11-09T03:22:46.829Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/608752/\">608752/</a>                                           2018-11-16T17:39:33.211Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/609904/\">609904/</a>                                           2018-11-21T05:56:27.967Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/615489/\">615489/</a>                                           2018-12-11T20:30:54.966Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/620317/\">620317/</a>                                           2019-01-09T15:29:42.588Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/622472/\">622472/</a>                                           2019-01-15T02:45:30.978Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/622871/\">622871/</a>                                           2019-01-15T20:21:39.642Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/623082/\">623082/</a>                                           2019-01-16T07:43:24.330Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/624487/\">624487/</a>                                           2019-01-22T18:59:41.234Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/624492/\">624492/</a>                                           2019-02-05T19:29:01.803Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/630727/\">630727/</a>                                           2019-02-11T22:08:17.428Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/637110/\">637110/</a>                                           2019-03-05T02:41:54.514Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/641577/\">641577/</a>                                           2019-03-29T05:40:34.645Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/648643/\">648643/</a>                                           2019-04-08T23:14:10.518Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/649004/\">649004/</a>                                           2019-04-09T05:45:16.731Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/650583/\">650583/</a>                                           2019-04-13T07:36:51.688Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/654752/\">654752/</a>                                           2019-04-29T03:26:38.726Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/656675/\">656675/</a>                                           2019-05-06T07:29:59.978Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/662092/\">662092/</a>                                           2019-05-22T19:24:54.428Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/665405/\">665405/</a>                                           2019-06-04T18:11:19.361Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/666595/\">666595/</a>                                           2019-06-06T17:49:18.411Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/668716/\">668716/</a>                                           2019-06-14T08:13:27.446Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/669486/\">669486/</a>                                           2019-06-15T23:37:58.899Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/672088/\">672088/</a>                                           2019-06-26T08:26:04.143Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/673587/\">673587/</a>                                           2019-07-01T09:09:26.829Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/674921/\">674921/</a>                                           2019-07-08T05:37:12.858Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/686378/\">686378/</a>                                           2019-09-13T07:43:14.649Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/706915/\">706915/</a>                                           2019-10-24T13:20:05.181Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/722234/\">722234/</a>                                           2020-01-27T14:58:07.611Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/737027/\">737027/</a>                                           2020-04-16T09:19:09.634Z                          -\n" +
                "<a href=\"/mirrors/chromium-browser-snapshots/Win_x64/756035/\">756035/</a>                                           2020-05-18T11:53:52.335Z                          -\n" +
                "</pre>\n" +
                "<hr>\n" +
                "\n" +
                "    </div>\n" +
                "\n" +
                "      <hr/>\n" +
                "      <div class=\"bottom\">\n" +
                "        Copyright &copy; <a href=\"https://github.com/cnpm\" target=\"_blank\">cnpm</a>\n" +
                "        |\n" +
                "        <a href=\"/\">Home</a>\n" +
                "    </div>\n" +
                "  </body>\n" +
                "</html>\n" +
                "\n";
        Pattern pattern = Pattern.compile("<a href=\"/mirrors/chromium-browser-snapshots/(.*)?/\">");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            matcher.find(matcher.end());
            System.out.println(matcher.end());
            String group = matcher.group(1);
            System.out.println(group);
        }

        List<String> sss= new ArrayList<>();
        sss.add("12");
        sss.add("13");
        sss.add("11");
        sss.add("10");
//        sss.stream().sorted(Comparator.reverseOrder());
        sss.sort(Comparator.reverseOrder());
        System.out.println(sss);

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        NashornScriptEngine nashorn = (NashornScriptEngine)scriptEngineManager.getEngineByName("nashorn");
         nashorn.eval("function validateFunction(functionText){\n" +
                 "  try {\n" +
                 "    new Function('(' + functionText + ')');\n" +
                 "  } catch (error) {\n" +
                 "    // This means we might have a function shorthand. Try another\n" +
                 "    // time prefixing 'function '.\n" +
                 "    if (functionText.startsWith('async '))\n" +
                 "      functionText =\n" +
                 "        'async function ' + functionText.substring('async '.length);\n" +
                 "    else functionText = 'function ' + functionText;\n" +
                 "    try {\n" +
                 "      new Function('(' + functionText + ')');\n" +
                 "    } catch (error) {\n" +
                 "      // We tried hard to serialize, but there's a weird beast here.\n" +
                 "      throw new Error('Passed function is not well-serializable!');\n" +
                 "    }\n" +
                 "  }\n" +
                 "  return functionText;\n" +
                 " }");
//
//        Bindings bindings = new SimpleBindings();
//        bindings.put("functionText","hhhhhhhhhh");
//        Object eval = nashorn.eval(bindings);
//        System.out.println(eval);
        Object o = nashorn.invokeFunction("validateFunction", "() => {}");
        System.out.println(o);
    }
}
