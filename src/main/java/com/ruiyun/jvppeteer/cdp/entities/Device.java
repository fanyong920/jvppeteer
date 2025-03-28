package com.ruiyun.jvppeteer.cdp.entities;

/**
 * 可以模拟的手机设备枚举类
 */
public enum  Device {

    BLACKBERRY_PLAYBOOK("Blackberry PlayBook","Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",new Viewport(600,1024,1.00,true,true,false)),
    BLACKBERRY_PLAYBOOK_LANDSCAPE("Blackberry PlayBook landscape","Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",new Viewport(1024,600,1.00,true,true,true)),
    BLACKBERRY_Z30("BlackBerry Z30","Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",new Viewport(360,640,2.00,true,true,false)),
    BLACKBERRY_Z30_LANDSCAPE("BlackBerry Z30 landscape","Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",new Viewport(640,360,2.00,true,true,true)),
    GALAXY_NOTE_3("Galaxy Note 3","Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360,640,3.00,true,true,false)),
    GALAXY_NOTE_3_LANDSCAPE("Galaxy Note 3 landscape","Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640,360,3.00,true,true,true)),
    GALAXY_NOTE_II("Galaxy Note II","Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360,640,2.00,true,true,false)),
    GALAXY_NOTE_II_LANDSCAPE("Galaxy Note II landscape","Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640,360,2.00,true,true,true)),
    GALAXY_S_III("Galaxy S III","Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360,640,2.00,true,true,false)),
    GALAXY_S_III_LANDSCAPE("Galaxy S III landscape","Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640,360,2.00,true,true,true)),
    GALAXY_S5("Galaxy S5","Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(360,640,3.00,true,true,false)),
    GALAXY_S5_LANDSCAPE("Galaxy S5 landscape","Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640,360,3.00,true,true,true)),
    GALAXY_S8("Galaxy S8","Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36",new Viewport(360,740,3.00,true,true,false)),
    GALAXY_S8_LANDSCAPE("Galaxy S8 landscape","Mozilla/5.0 (Linux; Android 7.0; SM-G950U Build/NRD90M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.84 Mobile Safari/537.36",new Viewport(740,360,3.00,true,true,true)),
    Galaxy_S9("Galaxy S9+","Mozilla/5.0 (Linux; Android 8.0.0; SM-G965U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36",new Viewport(320,658,4.5,true,true,false)),
    Galaxy_S9_LANDSCAPE("Galaxy S9+ landscape","Mozilla/5.0 (Linux; Android 8.0.0; SM-G965U Build/R16NW) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.111 Mobile Safari/537.36",new Viewport(658,320,4.5,true,true,true)),
    Galaxy_Tab_S4("Galaxy Tab S4","Mozilla/5.0 (Linux; Android 8.1.0; SM-T837A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.80 Safari/537.36",new Viewport(712,1138,2.25,true,true,false)),
    Galaxy_Tab_S4_LANDSCAPE("Galaxy Tab S4 landscape","Mozilla/5.0 (Linux; Android 8.1.0; SM-T837A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.80 Safari/537.36",new Viewport(1138,712,2.25,true,true,true)),
    IPAD("iPad","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(768,1024,2.00,true,true,false)),
    IPAD_LANDSCAPE("iPad landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024,768,2.00,true,true,true)),
    IPAD_GEN_6("iPad (gen 6)","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(768,1024,2.00,true,true,false)),
    IPAD_GEN_6_LANDSCAPE("iPad (gen 6) landscape","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(1024,768,2.00,true,true,true)),
    IPAD_GEN_7("iPad (gen 7)","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(810,1080,2.00,true,true,false)),
    IPAD_GEN_7_LANDSCAPE("iPad (gen 7) landscape","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(1080,810,2.00,true,true,true)),
    IPAD_MINI("iPad Mini","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(768,1024,2.00,true,true,false)),
    IPAD_MINI_LANDSCAPE("iPad Mini landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024,768,2.00,true,true,true)),
    IPAD_PRO("iPad Pro","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024,1366,2.00,true,true,false)),
    IPAD_PRO_LANDSCAPE("iPad Pro landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1366,1024,2.00,true,true,true)),
    IPAD_PRO_11("iPad Pro 11","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(834,1194,2.00,true,true,false)),
    IPAD_PRO_11_LANDSCAPE("iPad Pro 11 landscape","Mozilla/5.0 (iPad; CPU OS 12_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(1194,834,2.00,true,true,true)),
    IPHONE_4("iPhone 4","Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53",new Viewport(320,480,2.00,true,true,false)),
    IPHONE_4_LANDSCAPE("iPhone 4 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53",new Viewport(480,320,2.00,true,true,true)),
    IPHONE_5("iPhone 5","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(320,568,2.00,true,true,false)),
    IPHONE_5_LANDSCAPE("iPhone 5 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(568,320,2.00,true,true,true)),
    IPHONE_6("iPhone 6","MoziKlla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375,667,2.00,true,true,false)),
    IPHONE_6_LANDSCAPE("iPhone 6 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667,375,2.00,true,true,true)),
    IPHONE_6_PLUS("iPhone 6 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414,736,3.00,true,true,false)),
    IPHONE_6_PLUS_LANDSCAPE("iPhone 6 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736,414,3.00,true,true,true)),
    IPHONE_7("iPhone 7","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375,667,2.00,true,true,false)),
    IPHONE_7_LANDSCAPE("iPhone 7 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667,375,2.00,true,true,true)),
    IPHONE_7_PLUS("iPhone 7 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414,736,3.00,true,true,false)),
    IPHONE_7_PLUS_LANDSCAPE("iPhone 7 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736,414,3.00,true,true,true)),
    IPHONE_8("iPhone 8","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375,667,2.00,true,true,false)),
    IPHONE_8_LANDSCAPE("iPhone 8 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667,375,2.00,true,true,true)),
    IPHONE_8_PLUS("iPhone 8 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414,736,3.00,true,true,false)),
    IPHONE_8_PLUS_LANDSCAPE("iPhone 8 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736,414,3.00,true,true,true)),
    IPHONE_SE("iPhone SE","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(320,568,2.00,true,true,false)),
    IPHONE_SE_LANDSCAPE("iPhone SE landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(568,320,2.00,true,true,true)),
    IPHONE_X("iPhone X","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375,812,3.00,true,true,false)),
    IPHONE_X_LANDSCAPE("iPhone X landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(812,375,3.00,true,true,true)),
    IPHONE_XR("iPhone XR","Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",new Viewport(414,896,3.00,true,true,false)),
    IPHONE_XR_LANDSCAPE("iPhone XR landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",new Viewport(896,414,3.00,true,true,true)),
    IPHONE_11("iPhone 11","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(414,828,2.00,true,true,false)),
    IPHONE_11_LANDSCAPE("iPhone 11 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(828,414,2.00,true,true,true)),
    IPHONE_11_PRO("iPhone 11 Pro","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(375,812,3.00,true,true,false)),
    IPHONE_11_PRO_LANDSCAPE("iPhone 11 Pro landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(812,375,3.00,true,true,true)),
    IPHONE_11_PRO_MAX("iPhone 11 Pro Max","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(414,896,3.00,true,true,false)),
    IPHONE_11_PRO_MAX_LANDSCAPE("iPhone 11 Pro Max landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.1 Mobile/15E148 Safari/604.1",new Viewport(896,414,3.00,true,true,true)),
    IPHONE_12("iPhone 12","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(390,844,3.00,true,true,false)),
    IPHONE_12_LANDSCAPE("iPhone 12 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(844,390,3.00,true,true,true)),
    IPHONE_12_PRO("iPhone 12 Pro","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(390,844,3.00,true,true,false)),
    IPHONE_12_PRO_LANDSCAPE("iPhone 12 Pro landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(844,390,3.00,true,true,true)),
    IPHONE_12_PRO_MAX("iPhone 12 Pro Max","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(428,926,3.00,true,true,false)),
    IPHONE_12_PRO_MAX_LANDSCAPE("iPhone 12 Pro Max landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(926,428,3.00,true,true,true)),
    IPHONE_12_MINI("iPhone 12 Mini","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(375,812,3.00,true,true,false)),
    IPHONE_12_MINI_LANDSCAPE("iPhone 12 Mini landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 14_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(812,375,3.00,true,true,true)),
    IPHONE_13("iPhone 13","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(390,844,3.00,true,true,false)),
    IPHONE_13_LANDSCAPE("iPhone 13 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(844,390,3.00,true,true,true)),
    IPHONE_13_PRO("iPhone 13 Pro","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(390,844,3.00,true,true,false)),
    IPHONE_13_PRO_LANDSCAPE("iPhone 13 Pro landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(844,390,3.00,true,true,true)),
    IPHONE_13_PRO_MAX("iPhone 13 Pro Max","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(428,926,3.00,true,true,false)),
    IPHONE_13_PRO_MAX_LANDSCAPE("iPhone 13 Pro Max landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(926,428,3.00,true,true,true)),
    IPHONE_13_MINI("iPhone 13 Mini","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(375,812,3.00,true,true,false)),
    IPHONE_13_MINI_LANDSCAPE("iPhone 13 Mini landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 15_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/15.4 Mobile/15E148 Safari/604.1",new Viewport(812,375,3.00,true,true,true)),
    IPHONE_14("iPhone 14","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(390,633,3.00,true,true,false)),
    IPHONE_14_LANDSCAPE("iPhone 14 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(750,340,3.00,true,true,true)),
    IPHONE_14_PLUS("iPhone 14 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(428,745,3.00,true,true,false)),
    IPHONE_14__PLUS_LANDSCAPE("iPhone 14 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(832,378,3.00,true,true,true)),
    IPHONE_14_PRO("iPhone 14 Pro","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(393,659,3.00,true,true,false)),
    IPHONE_14_PRO_LANDSCAPE("iPhone 14 Pro landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(734,343,3.00,true,true,true)),
    IPHONE_14_PRO_MAX("iPhone 14 Pro Max","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(430,739,3.00,true,true,false)),
    IPHONE_14_PRO_MAX_LANDSCAPE("iPhone 14 Pro Max landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1",new Viewport(814,380,3.00,true,true,true)),
    PHONE_15("iPhone 15","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(393,659,3.00,true,true,false)),
    IPHONE_15_LANDSCAPE("iPhone 15 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(734,343,3.00,true,true,true)),
    IPHONE_15_PLUS("iPhone 15 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(430,739,3.00,true,true,false)),
    IPHONE_15__PLUS_LANDSCAPE("iPhone 15 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(814,380,3.00,true,true,true)),
    IPHONE_15_PRO("iPhone 15 Pro","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(393,659,3.00,true,true,false)),
    IPHONE_15_PRO_LANDSCAPE("iPhone 15 Pro landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(734,343,3.00,true,true,true)),
    IPHONE_15_PRO_MAX("iPhone 15 Pro Max","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(430,739,3.00,true,true,false)),
    IPHONE_15_PRO_MAX_LANDSCAPE("iPhone 15 Pro Max landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",new Viewport(814,380,3.00,true,true,true)),
    JIOPHONE_2("JioPhone 2","Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",new Viewport(240,320,1.00,true,true,false)),
    JIOPHONE_2_LANDSCAPE("JioPhone 2 landscape","Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",new Viewport(320,240,1.00,true,true,true)),
    KINDLE_FIRE_HDX("Kindle Fire HDX","Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",new Viewport(800,1280,2.00,true,true,false)),
    KINDLE_FIRE_HDX_LANDSCAPE("Kindle Fire HDX landscape","Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",new Viewport(1280,800,2.00,true,true,true)),
    LG_OPTIMUS_L70("LG Optimus L70","Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(384,640,1.25,true,true,false)),
    LG_OPTIMUS_L70_LANDSCAPE("LG Optimus L70 landscape","Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640,384,1.25,true,true,true)),
    MICROSOFT_LUMIA_550("Microsoft Lumia 550","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 550) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(640,360,2.00,true,true,false)),
    MICROSOFT_LUMIA_950("Microsoft Lumia 950","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(360,640,4.00,true,true,false)),
    MICROSOFT_LUMIA_950_LANDSCAPE("Microsoft Lumia 950 landscape","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(640,360,4.00,true,true,true)),
    NEXUS_10("Nexus 10","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(800,1280,2.00,true,true,false)),
    NEXUS_10_LANDSCAPE("Nexus 10 landscape","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(1280,800,2.00,true,true,true)),
    NEXUS_4("Nexus 4","Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(384,640,2.00,true,true,false)),
    NEXUS_4_LANDSCAPE("Nexus 4 landscape","Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640,384,2.00,true,true,true)),
    NEXUS_5("Nexus 5","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(360,640,3.00,true,true,false)),
    NEXUS_5_LANDSCAPE("Nexus 5 landscape","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640,360,3.00,true,true,true)),
    NEXUS_5X("Nexus 5X","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412,732,2.625,true,true,false)),
    NEXUS_5X_LANDSCAPE("Nexus 5X landscape","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732,412,2.625,true,true,true)),
    NEXUS_6("Nexus 6","Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412,732,3.5,true,true,false)),
    NEXUS_6_LANDSCAPE("Nexus 6 landscape","Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732,412,3.5,true,true,true)),
    NEXUS_6P("Nexus 6P","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412,732,3.5,true,true,false)),
    NEXUS_6P_LANDSCAPE("Nexus 6P landscape","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732,412,3.5,true,true,true)),
    NEXUS_7("Nexus 7","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(600,960,2.00,true,true,false)),
    NEXUS_7_LANDSCAPE("Nexus 7 landscape","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(960,600,2.00,true,true,true)),
    NOKIA_LUMIA_520("Nokia Lumia 520","Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",new Viewport(320,533,1.5,true,true,false)),
    NOKIA_LUMIA_520_LANDSCAPE("Nokia Lumia 520 landscape","Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",new Viewport(533,320,1.5,true,true,true)),
    NOKIA_N9("Nokia N9","Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",new Viewport(480,854,1.00,true,true,false)),
    NOKIA_N9_LANDSCAPE("Nokia N9 landscape","Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",new Viewport(854,480,1.00,true,true,true)),
    PIXEL_2("Pixel 2","Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(411,731,2.625,true,true,false)),
    PIXEL_2_LANDSCAPE("Pixel 2 landscape","Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(731,411,2.625,true,true,true)),
    PIXEL_2_XL("Pixel 2 XL","Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(411,823,3.5,true,true,false)),
    PIXEL_2_XL_LANDSCAPE("Pixel 2 XL landscape","Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(823,411,3.5,true,true,true)),
    PIXEL_3("Pixel 3","Mozilla/5.0 (Linux; Android 9; Pixel 3 Build/PQ1A.181105.017.A1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.158 Mobile Safari/537.36",new Viewport(393,786,2.75,true,true,false)),
    PIXEL_3_LANDSCAPE("Pixel 3 landscape","Mozilla/5.0 (Linux; Android 9; Pixel 3 Build/PQ1A.181105.017.A1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.158 Mobile Safari/537.36",new Viewport(786,393,2.75,true,true,true)),
    PIXEL_4("Pixel 4","Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36",new Viewport(353,745,3.00,true,true,false)),
    PIXEL_4_LANDSCAPE("Pixel 4 landscape","Mozilla/5.0 (Linux; Android 10; Pixel 4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Mobile Safari/537.36",new Viewport(745,353,3.00,true,true,true)),
    PIXEL_4A_5G("Pixel 4a (5G)","Mozilla/5.0 (Linux; Android 11; Pixel 4a (5G)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(353,745,3.00,true,true,false)),
    PIXEL_4A_5G_LANDSCAPE("Pixel 4a (5G) landscape","Mozilla/5.0 (Linux; Android 11; Pixel 4a (5G)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(745,353,3.00,true,true,true)),
    PIXEL_5("Pixel 5","Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(393,851,3.00,true,true,false)),
    PIXEL_5_LANDSCAPE("Pixel 5 landscape","Mozilla/5.0 (Linux; Android 11; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(851,393,3.00,true,true,true)),
    MOTO_G4("Moto G4","Mozilla/5.0 (Linux; Android 7.0; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(360,640,3.00,true,true,false)),
    MOTO_G4_LANDSCAPE("Moto G4 landscape","Mozilla/5.0 (Linux; Android 7.0; Moto G (4)) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4812.0 Mobile Safari/537.36",new Viewport(640,360,3.00,true,true,true)),

    //添加华为手机,具体屏幕手机尺寸不准确，但是不影响使用
    HUAWEI_MATE_30_PRO("HUAWEI Mate 30 Pro","Mozilla/5.0(Linux; U; Android 8.0.0; zh-CN; HUAWEI Mate 30 Pro Build/Mate 30 Pro)AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043024 Safari/537.36 MicroMessenger/6.3.16.49_r03ae324.780 NetType",new Viewport(350,900,3.5,true,true,false));

    Device(String name,String userAgent,Viewport viewPort) {
        this.name = name;
        this.userAgent = userAgent;
        this.viewport = viewPort;
    }
    private String name;

    private String userAgent;

    private Viewport viewport;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
