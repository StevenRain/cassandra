package com.cassandra.utils;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.net.ssl.SSLContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@Slf4j
public class HttpUtils extends SSLUtils {

    private static HttpClient client = null;
    private static final int TIME_OUT_IN_MILLISECONDS = 10000;

    private static HttpClient getHttpClient() {
        PoolingHttpClientConnectionManager connManager;
        if(Objects.nonNull(client)) {
            return client;
        }

        try {
            SSLContext sslcontext = createIgnoreVerifySSL();
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslcontext))
                    .build();
            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(TIME_OUT_IN_MILLISECONDS)
                    .setSocketTimeout(TIME_OUT_IN_MILLISECONDS)
                    .setConnectionRequestTimeout(TIME_OUT_IN_MILLISECONDS)
                    .build();
            client = HttpClients.custom().setDefaultRequestConfig(requestConfig).setConnectionManager(connManager).build();
        }catch (Exception e) {
            log.error("{}", e);
        }
        return client;
    }

    public static String sendPostByFormData(String url, Map<String, String> headerMap, Map<String, String> parameterMap) {
        try{
            HttpPost post = new HttpPost(url);
            List<NameValuePair> urlParameters = parameterMap.entrySet()
                    .stream()
                    .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());

            headerMap.forEach(post::setHeader);
            if(!headerMap.containsKey("User-Agent")) {
                post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            }
            post.setEntity(new UrlEncodedFormEntity(urlParameters));
            HttpResponse response = getHttpClient().execute(post);
            return EntityUtils.toString(response.getEntity());
        }catch (SocketTimeoutException e1) {
            log.error("Socket读取超时");
            return "";
        } catch (ConnectionPoolTimeoutException e2) {
            log.error("连接池超时");
            return "";
        }catch (Exception e) {
            log.error("{}", e);
            return "";
        }
    }

    public static String sendPostByJsonData(String url, Map<String, String> headerMap, String payload) {
        try{
            HttpPost post = new HttpPost(url);

            headerMap.forEach(post::setHeader);
            if(!headerMap.containsKey("User-Agent")) {
                post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            }

            StringEntity stringEntity = new StringEntity(payload);
            post.setEntity(stringEntity);

            HttpResponse response = getHttpClient().execute(post);
            return EntityUtils.toString(response.getEntity());
        }catch (SocketTimeoutException e1) {
            log.error("Socket读取超时");
            return "";
        } catch (ConnectionPoolTimeoutException e2) {
            log.error("连接池超时");
            return "";
        }catch (Exception e) {
            log.error("{}", e);
            return "";
        }
    }


    public static String sendGet(String url) {
        try{
            HttpGet get = new HttpGet(url);
            get.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
            HttpResponse response = getHttpClient().execute(get);
            return EntityUtils.toString(response.getEntity());
        }catch (UnknownHostException e1) {
            log.warn("解析URL地址 {} 失败", url);
            return "";
        }catch (ConnectionPoolTimeoutException e2) {
            log.warn("服务器连接超时");
            return "";
        } catch (SocketTimeoutException se) {
            log.warn("Socket读取超时");
            return "";
        } catch(Exception e) {
            log.error("{}", e);
            return "";
        }
    }
}
