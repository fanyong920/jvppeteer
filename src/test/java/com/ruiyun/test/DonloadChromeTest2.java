package com.ruiyun.test;

import java.io.IOException;

public class DonloadChromeTest2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        DownLoadUtil.download("https://storage.googleapis.com/chromium-browser-snapshots/Win_x64/737027/chrome-win.zip","D:\\develop\\project\\toString");
    }
}
