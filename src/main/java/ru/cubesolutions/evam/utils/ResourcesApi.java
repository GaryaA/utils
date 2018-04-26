package ru.cubesolutions.evam.utils;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;

import static ru.cubesolutions.evam.utils.CommonUtils.isNotNull;

/**
 * Created by Garya on 24.10.2017.
 */
public class ResourcesApi {

    private final static Logger log = Logger.getLogger(ResourcesApi.class);

    private ResourcesApi() {
    }

    public static void main(String[] args) {
        String url = "https://api.sendsay.ru/";

        sendGet(url);
    }

    public static String sendPost(String url, String data, String contentType) {
        return sendPost(url, data, contentType, null);
    }

    public static String sendPost(String url, String data, String contentType, ProxyObj proxyObj) {
        try (CloseableHttpClient client = initClient(proxyObj)) {
            HttpPost method = new HttpPost(url);
            method.setHeader("Content-type", contentType);
            method.setEntity(new StringEntity(data, "UTF-8"));
            RequestConfig rc = initRequestConfig(proxyObj);
            if (isNotNull(rc)) {
                method.setConfig(rc);
            }
            return getResponseString(client, method);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public static String sendGet(String url) {
        return sendGet(url, null);
    }

    public static String sendGet(String url, ProxyObj proxyObj) {
        try (CloseableHttpClient client = initClient(proxyObj)) {
            HttpGet method = new HttpGet(url);
            RequestConfig rc = initRequestConfig(proxyObj);
            if (isNotNull(rc)) {
                method.setConfig(rc);
            }
            return getResponseString(client, method);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public static void checkNotNullResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("response is null");
        }
    }

    private static String getResponseString(CloseableHttpClient client, HttpRequestBase method) throws IOException {
        log.debug("Executing request " + method.getRequestLine() + " to " + method.getURI().toString());
        try (CloseableHttpResponse response = client.execute(method)) {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                log.error("Error sending request with url: " + method.getURI().toString());
                log.error("Response status message:" + response.getStatusLine().getReasonPhrase());
                log.error("Resonse: " + getResponseString(response.getEntity().getContent()));
                throw new RuntimeException(responseCode + " code, Error sending request with url: " + method.getURI().toString());
            }
            return getResponseString(response.getEntity().getContent());
        }
    }

    private static String getResponseString(InputStream is) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            log.debug("Response from service: " + response.toString());
            return response.toString();
        }
    }

    private static CloseableHttpClient initClient(ProxyObj proxyObj) {
        if (isNotNull(proxyObj)) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyObj.getHost(), proxyObj.getPort()),
                    new UsernamePasswordCredentials(proxyObj.getUser(), proxyObj.getPassword()));
            return HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
        }
        return HttpClients.custom().build();
    }

    private static RequestConfig initRequestConfig(ProxyObj proxyObj) {
        return isNotNull(proxyObj)
                ? RequestConfig.custom().setProxy(new HttpHost(proxyObj.getHost(), proxyObj.getPort())).build()
                : null;
    }
}
