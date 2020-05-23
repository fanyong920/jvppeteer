package com.ruiyun.example;

import com.ruiyun.jvppeteer.util.FileUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResolveSiblingTest {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("D:/huyaLive/HuyaClient/Net45/Config/","dasda","213123.txt");
        FileUtil.createNewFile(path.toFile());

//        Path config = path.resolveSibling("Net45");
//        System.out.println(config.toString());
        System.out.println(path.getFileName());
        System.out.println(path.toString());
    }
}
