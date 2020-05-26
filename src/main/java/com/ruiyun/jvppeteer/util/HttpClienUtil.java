package com.ruiyun.jvppeteer.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ruiyun.jvppeteer.core.Constant;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicLineParser;
import org.apache.http.message.LineParser;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.CharArrayBuffer;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.CodingErrorAction;

public class HttpClienUtil implements Constant {

    public static  final CloseableHttpClient httpclient = initializedHttpCient();

    private static CloseableHttpClient initializedHttpCient() {
        HttpMessageParserFactory<HttpResponse> responseParserFactory = new DefaultHttpResponseParserFactory() {

            @Override
            public HttpMessageParser<HttpResponse> create(
                    SessionInputBuffer buffer, MessageConstraints constraints) {
                LineParser lineParser = new BasicLineParser() {

                    @Override
                    public Header parseHeader(final CharArrayBuffer buffer) {
                        try {
                            return super.parseHeader(buffer);
                        } catch (ParseException ex) {
                            return new BasicHeader(buffer.toString(), null);
                        }
                    }

                };
                return new DefaultHttpResponseParser(
                        buffer, lineParser, DefaultHttpResponseFactory.INSTANCE, constraints) {

                    @Override
                    protected boolean reject(final CharArrayBuffer line, int count) {
                        // try to ignore all garbage preceding a status line infinitely
                        return false;
                    }

                };
            }

        };
        HttpMessageWriterFactory<HttpRequest> requestWriterFactory = new DefaultHttpRequestWriterFactory();

        // Use a custom connection factory to customize the process of
        // initialization of outgoing HTTP connections. Beside standard connection
        // configuration parameters HTTP connection factory can define message
        // parser / writer routines to be employed by individual connections.
        HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                requestWriterFactory, responseParserFactory);

        // Client HTTP connection objects when fully initialized can be bound to
        // an arbitrary network socket. The process of network socket initialization,
        // its connection to a remote address and binding to a local one is controlled
        // by a connection socket factory.

        // SSL context for secure connections can be created either based on
        // system or application specific properties.
        SSLContext sslcontext = SSLContexts.createSystemDefault();

        // Create a registry of custom connection socket factories for supported
        // protocol schemes.
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE)
                .register("https", new SSLConnectionSocketFactory(sslcontext))
                .build();


        // Create a connection manager with custom configuration.
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry, connFactory);

        // Create socket configuration
        SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .build();
        // Configure the connection manager to use socket configuration either
        // by default or for a specific host.
        connManager.setDefaultSocketConfig(socketConfig);
        // Validate connections after 1 sec of inactivity
        connManager.setValidateAfterInactivity(1000);

        // Create connection configuration
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setMalformedInputAction(CodingErrorAction.IGNORE)
                .setUnmappableInputAction(CodingErrorAction.IGNORE)
                .setCharset(Consts.UTF_8)
                .build();
        // Configure the connection manager to use connection configuration either
        // by default or for a specific host.
        connManager.setDefaultConnectionConfig(connectionConfig);

        // Configure total max or per route limits for persistent connections
        // that can be kept in the pool or leased by the connection manager.
        connManager.setMaxTotal(50);
        connManager.setDefaultMaxPerRoute(10);
        // Create an HttpClient with the given custom dependencies and configuration.
        CloseableHttpClient httpclient = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();
        return httpclient;
    }

    public final static <T> T request(String url, Class<T> clazz,String host,int port) throws IOException {
            HttpGet httpget = new HttpGet(url);
            // Request configuration can be overridden at the request level.
            // They will take precedence over the one set at the client level.
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .build();
            httpget.setConfig(requestConfig);
            HttpHost httpHost = new HttpHost(host,port);
        try (org.apache.http.client.methods.CloseableHttpResponse response = httpclient.execute(httpHost, httpget)) {
            org.apache.http.StatusLine statusLine = response.getStatusLine();
            if (200 == statusLine.getStatusCode()) {
                return OBJECTMAPPER.readValue(org.apache.http.util.EntityUtils.toString(response.getEntity()), clazz);
            }
            String message =
                    java.text.MessageFormat.format(
                            "Request {0} Unexpected response status:{1}",
                            url, statusLine);
            throw new org.apache.http.client.ClientProtocolException(message);
        }
    }

    public final static <T> T request(String url, TypeReference<T> valueTypeRef,String host,int port) throws IOException {
        HttpGet httpget = new HttpGet(url);
        // Request configuration can be overridden at the request level.
        // They will take precedence over the one set at the client level.
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .build();
        httpget.setConfig(requestConfig);
        HttpHost httpHost = new HttpHost(host,port);
        try (org.apache.http.client.methods.CloseableHttpResponse response = httpclient.execute(httpHost, httpget)) {
            org.apache.http.StatusLine statusLine = response.getStatusLine();
            if (200 == statusLine.getStatusCode()) {
                String s = org.apache.http.util.EntityUtils.toString(response.getEntity());
                return OBJECTMAPPER.readValue(s, valueTypeRef);
            }
            String message =
                    java.text.MessageFormat.format(
                            "Request {0} Unexpected response status:{1}",
                            url, statusLine);
            throw new org.apache.http.client.ClientProtocolException(message);
        }
    }
    //TypeReference valueTypeRef
}
