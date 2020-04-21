package com.ruiyun.test;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.client.methods.ZeroCopyPost;

import java.io.File;
import java.util.concurrent.Future;

public class ZeroCopyHttpExchange {
    public static void main(final String[] args) throws Exception {
        try (org.apache.http.impl.nio.client.CloseableHttpAsyncClient httpclient = org.apache.http.impl.nio.client.HttpAsyncClients.createDefault()) {
            httpclient.start();
            java.io.File upload = new java.io.File(args[0]);
            java.io.File download = new java.io.File(args[1]);
            org.apache.http.nio.client.methods.ZeroCopyPost httpost = new org.apache.http.nio.client.methods.ZeroCopyPost("http://localhost:8080/", upload,
                    org.apache.http.entity.ContentType.create("text/plain"));
            org.apache.http.nio.client.methods.ZeroCopyConsumer<java.io.File> consumer = new org.apache.http.nio.client.methods.ZeroCopyConsumer<java.io.File>(download) {

                @Override
                protected java.io.File process(
                        final org.apache.http.HttpResponse response,
                        final java.io.File file,
                        final org.apache.http.entity.ContentType contentType) throws Exception {
                    if (response.getStatusLine().getStatusCode() != org.apache.http.HttpStatus.SC_OK) {
                        throw new org.apache.http.client.ClientProtocolException("Upload failed: " + response.getStatusLine());
                    }
                    return file;
                }

            };
            java.util.concurrent.Future<java.io.File> future = httpclient.execute(httpost, consumer, null);
            java.io.File result = future.get();
            System.out.println("Response file length: " + result.length());
            System.out.println("Shutting down");
        }
        System.out.println("Done");
    }
}
