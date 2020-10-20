package com.ruiyun.example.sometest;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MIMETypeTest {
    public static void main(String[] args) throws IOException {
        System.out.println(Files.probeContentType(Paths.get("/csdnimg.cn/medal/chizhiyiheng@120.png")));
    }

    @Test
    public  void test1()  {
        System.out.println(Paths.get(".local-browser\\win64-722234\\chrome-win\\chrome.exe").normalize().toAbsolutePath().toString());
    }
}
