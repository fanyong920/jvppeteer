package com.ruiyun.example.sometest;

import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DirectBufferTest {

    @Test
    public  void test1() throws IOException {
        long start = System.currentTimeMillis();
        Path path = Paths.get("E:\\lolvideo\\1023079644\\21s.avi");
        FileChannel open = FileChannel.open(path);
        long size = open.size();
        MappedByteBuffer buffer = open.map(FileChannel.MapMode.READ_ONLY, 0, size);
        for (int i = 0; i < buffer.limit(); i++) {
            byte b = buffer.get();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public  void test2() throws IOException {
        long start = System.currentTimeMillis();
        Path path = Paths.get("E:\\lolvideo\\1023079644\\21s.avi");
        FileChannel open = FileChannel.open(path);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(8192);
        int read ;
        while ((read = open.read(byteBuffer)) != -1){
            byteBuffer.clear();
        }
        long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

}
