package com.ruiyun.example.sometest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatch {
    public static void main(String[] args) throws IOException {
        List<String> strings = Files.readAllLines(Paths.get("C:\\Users\\howay\\Desktop\\content.txt"));

        String join = String.join("\n", strings);

        Pattern pattern = Pattern.compile("<dd class=\"read_num\">\n" +
                "                                <a href=\"(.*?)\"");
        Matcher matcher = pattern.matcher(join);
        while (matcher.find()){
            String group = matcher.group(1);
            System.out.println(group);
        }


    }
}
