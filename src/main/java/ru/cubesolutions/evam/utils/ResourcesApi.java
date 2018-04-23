package ru.cubesolutions.evam.utils;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static ru.cubesolutions.evam.utils.CommonUtils.isNotNull;

/**
 * Created by Garya on 24.10.2017.
 */
public class ResourcesApi {

    private final static Logger log = Logger.getLogger(ResourcesApi.class);

    private ResourcesApi() {
    }

    public static String sendPost(String url, String data, String contentType) {
        return sendPost(url, data, contentType, null);
    }

    public static String sendPost(String url, String data, String contentType, ProxyObj proxyObj) {
        HttpClient client = initClient(proxyObj);
        try {
            HttpPost method = new HttpPost(url);
            method.setHeader("Content-type", contentType);
            method.setEntity(new StringEntity(data, "UTF-8"));
            return getResponseString(client, method);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static String sendGet(String url) {
        return sendGet(url, null);
    }

    public static String sendGet(String url, ProxyObj proxyObj) {
        HttpClient client = initClient(proxyObj);
        try {
            HttpGet method = new HttpGet(url);
            return getResponseString(client, method);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        } finally {
            client.getConnectionManager().shutdown();
        }
    }

    public static void checkNotNullResponse(String response) {
        if (response == null || response.isEmpty()) {
            throw new RuntimeException("response is null");
        }
    }

    private static String getResponseString(HttpClient client, HttpRequestBase method) throws IOException {
        log.debug("Executing request " + method.getRequestLine() + " to " + method.getURI().toString());
        HttpResponse response = client.execute(method);
        try {
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != 200) {
                log.error("Error sending request with url: " + method.getURI().toString());
                log.error("Response status message:" + response.getStatusLine().getReasonPhrase());
                log.error("Resonse: " + getResponseString(response.getEntity().getContent()));
                throw new RuntimeException(responseCode + " code, Error sending request with url: " + method.getURI().toString());
            }
            return getResponseString(response.getEntity().getContent());
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
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

    private static HttpClient initClient(ProxyObj proxyObj) {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        if (isNotNull(proxyObj)) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyObj.getHost(), proxyObj.getPort()),
                    new UsernamePasswordCredentials(proxyObj.getUser(), proxyObj.getPassword()));
            httpClient.setCredentialsProvider(credsProvider);
            HttpHost proxy = new HttpHost(proxyObj.getHost(), proxyObj.getPort(), (proxyObj.getHost().indexOf("https") != 0) ? "http" : "https");
            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        }
        return httpClient;
    }

}
