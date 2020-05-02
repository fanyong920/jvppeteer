package com.ruiyun.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MIMETypeTest {
    public static void main(String[] args) throws IOException {
        System.out.println(Files.probeContentType(Paths.get("/csdnimg.cn/medal/chizhiyiheng@120.png")));
    }
}
