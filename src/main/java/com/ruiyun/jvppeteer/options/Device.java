package com.ruiyun.jvppeteer.options;

/**
 * 可以模拟的手机设备
 */
public enum  Device {

    BLACKBERRY_PLAYBOOK("Blackberry PlayBook","Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",new Viewport(600.00,1024.00,1.00,true,true,false)),
    BLACKBERRY_PLAYBOOK_LANDSCAPE("Blackberry PlayBook landscape","Mozilla/5.0 (PlayBook; U; RIM Tablet OS 2.1.0; en-US) AppleWebKit/536.2+ (KHTML like Gecko) Version/7.2.1.0 Safari/536.2+",new Viewport(1024.00,600.00,1.00,true,true,true)),
    BLACKBERRY_Z30("BlackBerry Z30","Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",new Viewport(360.00,640.00,2.00,true,true,false)),
    BLACKBERRY_Z30_LANDSCAPE("BlackBerry Z30 landscape","Mozilla/5.0 (BB10; Touch) AppleWebKit/537.10+ (KHTML, like Gecko) Version/10.0.9.2372 Mobile Safari/537.10+",new Viewport(640.00,360.00,2.00,true,true,true)),
    GALAXY_NOTE_3("Galaxy Note 3","Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360.00,640.00,3.00,true,true,false)),
    GALAXY_NOTE_3_LANDSCAPE("Galaxy Note 3 landscape","Mozilla/5.0 (Linux; U; Android 4.3; en-us; SM-N900T Build/JSS15J) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640.00,360.00,3.00,true,true,true)),
    GALAXY_NOTE_II("Galaxy Note II","Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360.00,640.00,2.00,true,true,false)),
    GALAXY_NOTE_II_LANDSCAPE("Galaxy Note II landscape","Mozilla/5.0 (Linux; U; Android 4.1; en-us; GT-N7100 Build/JRO03C) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640.00,360.00,2.00,true,true,true)),
    GALAXY_S_III("Galaxy S III","Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(360.00,640.00,2.00,true,true,false)),
    GALAXY_S_III_LANDSCAPE("Galaxy S III landscape","Mozilla/5.0 (Linux; U; Android 4.0; en-us; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30",new Viewport(640.00,360.00,2.00,true,true,true)),
    GALAXY_S5("Galaxy S5","Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(360.00,640.00,3.00,true,true,false)),
    GALAXY_S5_LANDSCAPE("Galaxy S5 landscape","Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640.00,360.00,3.00,true,true,true)),
    IPAD("iPad","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(768.00,1024.00,2.00,true,true,false)),
    IPAD_LANDSCAPE("iPad landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024.00,768.00,2.00,true,true,true)),
    IPAD_MINI("iPad Mini","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(768.00,1024.00,2.00,true,true,false)),
    IPAD_MINI_LANDSCAPE("iPad Mini landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024.00,768.00,2.00,true,true,true)),
    IPAD_PRO("iPad Pro","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1024.00,1366.00,2.00,true,true,false)),
    IPAD_PRO_LANDSCAPE("iPad Pro landscape","Mozilla/5.0 (iPad; CPU OS 11_0 like Mac OS X) AppleWebKit/604.1.34 (KHTML, like Gecko) Version/11.0 Mobile/15A5341f Safari/604.1",new Viewport(1366.00,1024.00,2.00,true,true,true)),
    IPHONE_4("iPhone 4","Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53",new Viewport(320.00,480.00,2.00,true,true,false)),
    IPHONE_4_LANDSCAPE("iPhone 4 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53",new Viewport(480.00,320.00,2.00,true,true,true)),
    IPHONE_5("iPhone 5","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(320.00,568.00,2.00,true,true,false)),
    IPHONE_5_LANDSCAPE("iPhone 5 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(568.00,320.00,2.00,true,true,true)),
    IPHONE_6("iPhone 6","MoziKlla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375.00,667.00,2.00,true,true,false)),
    IPHONE_6_LANDSCAPE("iPhone 6 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667.00,375.00,2.00,true,true,true)),
    IPHONE_6_PLUS("iPhone 6 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414.00,736.00,3.00,true,true,false)),
    IPHONE_6_PLUS_LANDSCAPE("iPhone 6 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736.00,414.00,3.00,true,true,true)),
    IPHONE_7("iPhone 7","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375.00,667.00,2.00,true,true,false)),
    IPHONE_7_LANDSCAPE("iPhone 7 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667.00,375.00,2.00,true,true,true)),
    IPHONE_7_PLUS("iPhone 7 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414.00,736.00,3.00,true,true,false)),
    IPHONE_7_PLUS_LANDSCAPE("iPhone 7 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736.00,414.00,3.00,true,true,true)),
    IPHONE_8("iPhone 8","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375.00,667.00,2.00,true,true,false)),
    IPHONE_8_LANDSCAPE("iPhone 8 landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(667.00,375.00,2.00,true,true,true)),
    IPHONE_8_PLUS("iPhone 8 Plus","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(414.00,736.00,3.00,true,true,false)),
    IPHONE_8_PLUS_LANDSCAPE("iPhone 8 Plus landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(736.00,414.00,3.00,true,true,true)),
    IPHONE_SE("iPhone SE","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(320.00,568.00,2.00,true,true,false)),
    IPHONE_SE_LANDSCAPE("iPhone SE landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1",new Viewport(568.00,320.00,2.00,true,true,true)),
    IPHONE_X("iPhone X","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(375.00,812.00,3.00,true,true,false)),
    IPHONE_X_LANDSCAPE("iPhone X landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1",new Viewport(812.00,375.00,3.00,true,true,true)),
    IPHONE_XR("iPhone XR","Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",new Viewport(414.00,896.00,3.00,true,true,false)),
    IPHONE_XR_LANDSCAPE("iPhone XR landscape","Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1",new Viewport(896.00,414.00,3.00,true,true,true)),
    JIOPHONE_2("JioPhone 2","Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",new Viewport(240.00,320.00,1.00,true,true,false)),
    JIOPHONE_2_LANDSCAPE("JioPhone 2 landscape","Mozilla/5.0 (Mobile; LYF/F300B/LYF-F300B-001-01-15-130718-i;Android; rv:48.0) Gecko/48.0 Firefox/48.0 KAIOS/2.5",new Viewport(320.00,240.00,1.00,true,true,true)),
    KINDLE_FIRE_HDX("Kindle Fire HDX","Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",new Viewport(800.00,1280.00,2.00,true,true,false)),
    KINDLE_FIRE_HDX_LANDSCAPE("Kindle Fire HDX landscape","Mozilla/5.0 (Linux; U; en-us; KFAPWI Build/JDQ39) AppleWebKit/535.19 (KHTML, like Gecko) Silk/3.13 Safari/535.19 Silk-Accelerated=true",new Viewport(1280.00,800.00,2.00,true,true,true)),
    LG_OPTIMUS_L70("LG Optimus L70","Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(384.00,640.00,1.25,true,true,false)),
    LG_OPTIMUS_L70_LANDSCAPE("LG Optimus L70 landscape","Mozilla/5.0 (Linux; U; Android 4.4.2; en-us; LGMS323 Build/KOT49I.MS32310c) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640.00,384.00,1.25,true,true,true)),
    MICROSOFT_LUMIA_550("Microsoft Lumia 550","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 550) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(640.00,360.00,2.00,true,true,false)),
    MICROSOFT_LUMIA_950("Microsoft Lumia 950","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(360.00,640.00,4.00,true,true,false)),
    MICROSOFT_LUMIA_950_LANDSCAPE("Microsoft Lumia 950 landscape","Mozilla/5.0 (Windows Phone 10.0; Android 4.2.1; Microsoft; Lumia 950) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Mobile Safari/537.36 Edge/14.14263",new Viewport(640.00,360.00,4.00,true,true,true)),
    NEXUS_10("Nexus 10","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(800.00,1280.00,2.00,true,true,false)),
    NEXUS_10_LANDSCAPE("Nexus 10 landscape","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 10 Build/MOB31T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(1280.00,800.00,2.00,true,true,true)),
    NEXUS_4("Nexus 4","Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(384.00,640.00,2.00,true,true,false)),
    NEXUS_4_LANDSCAPE("Nexus 4 landscape","Mozilla/5.0 (Linux; Android 4.4.2; Nexus 4 Build/KOT49H) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640.00,384.00,2.00,true,true,true)),
    NEXUS_5("Nexus 5","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(360.00,640.00,3.00,true,true,false)),
    NEXUS_5_LANDSCAPE("Nexus 5 landscape","Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(640.00,360.00,3.00,true,true,true)),
    NEXUS_5X("Nexus 5X","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412.00,732.00,2.625,true,true,false)),
    NEXUS_5X_LANDSCAPE("Nexus 5X landscape","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 5X Build/OPR4.170623.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732.00,412.00,2.625,true,true,true)),
    NEXUS_6("Nexus 6","Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412.00,732.00,3.5,true,true,false)),
    NEXUS_6_LANDSCAPE("Nexus 6 landscape","Mozilla/5.0 (Linux; Android 7.1.1; Nexus 6 Build/N6F26U) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732.00,412.00,3.5,true,true,true)),
    NEXUS_6P("Nexus 6P","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(412.00,732.00,3.5,true,true,false)),
    NEXUS_6P_LANDSCAPE("Nexus 6P landscape","Mozilla/5.0 (Linux; Android 8.0.0; Nexus 6P Build/OPP3.170518.006) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(732.00,412.00,3.5,true,true,true)),
    NEXUS_7("Nexus 7","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(600.00,960.00,2.00,true,true,false)),
    NEXUS_7_LANDSCAPE("Nexus 7 landscape","Mozilla/5.0 (Linux; Android 6.0.1; Nexus 7 Build/MOB30X) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Safari/537.36",new Viewport(960.00,600.00,2.00,true,true,true)),
    NOKIA_LUMIA_520("Nokia Lumia 520","Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",new Viewport(320.00,533.00,1.5,true,true,false)),
    NOKIA_LUMIA_520_LANDSCAPE("Nokia Lumia 520 landscape","Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; NOKIA; Lumia 520)",new Viewport(533.00,320.00,1.5,true,true,true)),
    NOKIA_N9("Nokia N9","Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",new Viewport(480.00,854.00,1.00,true,true,false)),
    NOKIA_N9_LANDSCAPE("Nokia N9 landscape","Mozilla/5.0 (MeeGo; NokiaN9) AppleWebKit/534.13 (KHTML, like Gecko) NokiaBrowser/8.5.0 Mobile Safari/534.13",new Viewport(854.00,480.00,1.00,true,true,true)),
    PIXEL_2("Pixel 2","Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(411.00,731.00,2.625,true,true,false)),
    PIXEL_2_LANDSCAPE("Pixel 2 landscape","Mozilla/5.0 (Linux; Android 8.0; Pixel 2 Build/OPD3.170816.012) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(731.00,411.00,2.625,true,true,true)),
    PIXEL_2_XL("Pixel 2 XL","Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(411.00,823.00,3.5,true,true,false)),
    PIXEL_2_XL_LANDSCAPE("Pixel 2 XL landscape","Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 XL Build/OPD1.170816.004) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3765.0 Mobile Safari/537.36",new Viewport(823.00,411.00,3.5,true,true,true));

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
