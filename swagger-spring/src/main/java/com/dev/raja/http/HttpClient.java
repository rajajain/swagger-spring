package com.dev.raja.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class.getCanonicalName());
    private static Map<String, CloseableHttpClient> httpClientMap = new ConcurrentHashMap<>();
    private static PoolingHttpClientConnectionManager poolingClientConnectionManager;
    private static ConnectionKeepAliveStrategy keepAliveStrategy;
    private static final int MAX_CONNECTIONS = 2000;
    private static final int MAX_CONNECTIONS_PER_ROUTE = 200;
    private static final int CONN_TIME_OUT = 6000;
    private static final int CONNECTION_TIMEOUT_MILLIS = 10000;
    private static final int SOCKET_TIMEOUT_MILLIS = 100000;

    public static final String USE_PROXY = "doc.gen.use.proxy";
    public static final String HTTP_PROXY_HOST = "doc.gen.http.proxy.host";
    public static final String HTTP_PROXY_PORT = "doc.gen.http.proxy.port";
    public static final String IS_HTTP_PROXY_SECURE = "doc.gen.http.is.proxy.secure";
    public static final String HTTP_PROXY_AUTH_ENABLED = "doc.gen.http.proxy.auth.enabled";
    public static final String HTTP_PROXY_AUTH_NAME = "doc.gen.http.proxy.auth.name";
    public static final String HTTP_PROXY_AUTH_PASSWORD = "doc.gen.http.proxy.auth.password";

    private static HttpHost proxy = null;

    // Create a trust manager that does not validate certificate chains
    static TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
    }};

    static {
        String proxyIP = System.getProperty(HTTP_PROXY_HOST);
        String proxyPort = System.getProperty(HTTP_PROXY_PORT);
        boolean isProxySecure = StringUtils.isNotBlank(System.getProperty(IS_HTTP_PROXY_SECURE))
                ? Boolean.valueOf(System.getProperty(IS_HTTP_PROXY_SECURE)) : false;
        if (StringUtils.isNotBlank(proxyIP) && StringUtils.isNotBlank(proxyPort)) {
            if (isProxySecure) {
                proxy = new HttpHost(proxyIP, Integer.parseInt(proxyPort), "https");
            } else {
                proxy = new HttpHost(proxyIP, Integer.parseInt(proxyPort), "http");
            }
        }
    }

    static {
        try {
            initConnectionManager();
        } catch (NoSuchAlgorithmException e) {
        } catch (KeyManagementException e) {
        } catch (KeyStoreException e) {
        } catch (URISyntaxException e) {
        }
    }

    /**
     * By default no need to use proxy.
     *
     * @param timeOut
     * @return
     */
    private static CloseableHttpClient getHttpClient(int timeOut) {
        return getHttpClient(timeOut, false);
    }

    /**
     * returns http client, useProxy param is used for proxy
     *
     * @param timeOut
     * @param useProxy boolean to use proxy
     * @return
     */
    private static CloseableHttpClient getHttpClient(int timeOut, boolean useProxy) {
        String key = useProxy == true ? "PROXY_" + timeOut : "NO_PROXY_" + timeOut;
        CloseableHttpClient httpClient = httpClientMap.get(key);
        if (httpClient == null) {
            CloseableHttpClient client;
            // Check for proxy configuration
            if (proxy != null && useProxy) {
                CredentialsProvider credentialsProvider = getHttpProxyCredentials();
                RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(20000).setSocketTimeout(timeOut).setConnectTimeout(CONN_TIME_OUT).setProxy(proxy).build();
                // Check for proxy authentication
                if (credentialsProvider != null) {
                    client = HttpClients.custom().setConnectionManager(poolingClientConnectionManager).setDefaultRequestConfig(config).setKeepAliveStrategy(keepAliveStrategy).evictIdleConnections(20L, TimeUnit.SECONDS).setDefaultCredentialsProvider(credentialsProvider).build();
                } else {
                    client = HttpClients.custom().setConnectionManager(poolingClientConnectionManager).setDefaultRequestConfig(config).setKeepAliveStrategy(keepAliveStrategy).evictIdleConnections(20L, TimeUnit.SECONDS).build();
                }
            } else {
                RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(20000).setSocketTimeout(timeOut).setConnectTimeout(CONN_TIME_OUT).build();
                client = HttpClients.custom().setConnectionManager(poolingClientConnectionManager).setDefaultRequestConfig(config).setKeepAliveStrategy(keepAliveStrategy).evictIdleConnections(20L, TimeUnit.SECONDS).build();
            }
            httpClientMap.put(key, client);
            return client;
        }
        return httpClient;
    }

    private static CredentialsProvider getHttpProxyCredentials() {
        boolean authEnabled = StringUtils.isNotBlank(System.getProperty(HTTP_PROXY_AUTH_ENABLED))
                ? Boolean.valueOf(System.getProperty(HTTP_PROXY_AUTH_ENABLED)) : false;
        if (authEnabled) {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    AuthScope.ANY,
                    new UsernamePasswordCredentials(System.getProperty(HTTP_PROXY_AUTH_NAME), System.getProperty(HTTP_PROXY_AUTH_PASSWORD)));
            return credsProvider;
        } else {
            return null;
        }
    }

    private static void initConnectionManager() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, URISyntaxException {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new TrustStrategy() {

            @Override
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).register("http", PlainConnectionSocketFactory.INSTANCE)
                .build();
        poolingClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

        poolingClientConnectionManager.setMaxTotal(MAX_CONNECTIONS);
        poolingClientConnectionManager.setDefaultMaxPerRoute(MAX_CONNECTIONS_PER_ROUTE);

        keepAliveStrategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                // Honor 'keep-alive' header
                HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value != null && param.equalsIgnoreCase("timeout")) {
                        try {
                            return Long.parseLong(value) * 1000;
                        } catch (NumberFormatException ignore) {
                        }
                    }
                }
                // otherwise keep alive for 30 seconds
                return 30 * 1000;
            }
        };
    }


    /**
     * User Apache HTTP Library
     *
     * @param url
     * @return
     */
    public static String getContent(String url) {
        HttpGet get = null;
        try {
            get = new HttpGet(url);
            HttpResponse response = getHttpClient(SOCKET_TIMEOUT_MILLIS).execute(get);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity httpEntity = response.getEntity();
            String responseStr = null;
            if (httpEntity != null) {
                responseStr = readContent(httpEntity);
            }

            if (statusLine.getStatusCode() / 100 == 2) {
                return responseStr;
            } else {
                if (statusLine.getStatusCode() >= 400) {
                    throw new Exception(String.format("Got Http error code [%s]", statusLine.getStatusCode()));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Got status code [{}] for url [{}]", statusLine.getStatusCode(), url);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error requesting url [{}] with timeout [{}]", new Object[]{url, SOCKET_TIMEOUT_MILLIS, e});
            logger.info("Connection Pool Stats:: [{}]", poolingClientConnectionManager.getTotalStats());


        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return null;
    }

    public static String getContent(String url, int timeOut) {
        return getContent(url, timeOut, false);
    }

    public static String getContent(String url, int timeOut, boolean useProxy) {

        HttpGet get = null;
        try {
            get = new HttpGet(url);

            HttpResponse response = getHttpClient(timeOut, useProxy).execute(get);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity httpEntity = response.getEntity();
            String responseStr = null;
            if (httpEntity != null) {
                responseStr = readContent(httpEntity);
            }

            if (statusLine.getStatusCode() / 100 == 2) {
                return responseStr;
            } else {
                if (statusLine.getStatusCode() >= 400) {

                    throw new Exception(String.format("Got Http error code [%s]", statusLine.getStatusCode()));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Got status code [{}] for url [{}]", statusLine.getStatusCode(), url);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error requesting url [{}] with timeout [{}]", new Object[]{url, timeOut, e});
            logger.info("Connection Pool Stats:: [{}]", poolingClientConnectionManager.getTotalStats());


        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return null;
    }

    private static String readContent(HttpEntity httpEntity) throws IOException {
        StringBuilder content = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                    httpEntity.getContent(), "UTF-8"));
            String line = null;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {

                }
            }
        }
    }

    public static String postData(String url, String data) throws Exception {
        return postData(url, data, false);
    }

    public static String postData(String url, String data, boolean useProxy) throws Exception {
        Map<String, String> headers = Collections.emptyMap();
        return postData(url, data, headers, useProxy);
    }

    public static String postData(String url, String data, Map<String, String> headers) throws Exception {
        return postData(url, data, headers, false);
    }

    public static String postData(String url, String data, Map<String, String> headers, boolean useProxy) throws Exception {
        return postData(url, data, headers, useProxy, SOCKET_TIMEOUT_MILLIS);
    }

    public static String postData(String url, String data, Map<String, String> headers, boolean useProxy, int timeout) throws Exception {
        HttpPost post = null;
        try {
            post = new HttpPost(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                post.setHeader(header.getKey(), header.getValue());
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
            HttpEntity entity = new InputStreamEntity(bis, bis.available());
            post.setEntity(entity);
            HttpResponse response = getHttpClient(timeout, useProxy).execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 400) {
                throw new Exception(String.format("Got Http error code [%s]", statusLine.getStatusCode()));
            } else {
                String responseString = IOUtil.readData(response.getEntity().getContent(), true);
                logger.info("Got status code [{}] for url [{}]", statusLine.getStatusCode(), url);
                return responseString;
            }
        } catch (Exception e) {
            logger.error("Error requesting url [{}] with timeout [{}]", new Object[]{url, SOCKET_TIMEOUT_MILLIS, e});

            throw e;
        } finally {
            if (post != null)
                post.releaseConnection();
        }
    }

    static HostnameVerifier hostnameVerifier = new HostnameVerifier() {

        public boolean verify(String urlHostName, SSLSession session) {
            System.out.println("Warning: URL Host: " + urlHostName + " vs. " + session.getPeerHost());
            return true;
        }
    };


    public static String getContent(String url, int timeOut, Map<String, String> headers)
            throws Exception {

        HttpGet get = null;
        try {
            get = new HttpGet(url);
            if (headers != null && headers.size() > 0) {
                Iterator<String> headerItr = headers.keySet().iterator();
                while (headerItr.hasNext()) {
                    String name = headerItr.next();
                    String value = headers.get(name);
                    get.addHeader(name, value);
                }
            }

            HttpResponse response = getHttpClient(timeOut).execute(get);
            StatusLine statusLine = response.getStatusLine();
            HttpEntity httpEntity = response.getEntity();
            String responseStr = null;
            if (httpEntity != null) {
                responseStr = readContent(httpEntity);
            }

            if (statusLine.getStatusCode() / 100 == 2) {
                return responseStr;
            } else {
                if (statusLine.getStatusCode() >= 400) {
                    throw new Exception(String.format("Got Http error code [%s]", statusLine.getStatusCode()));
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Got status code [{}] for url [{}]", statusLine.getStatusCode(), url);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error requesting url [{}] with timeout [{}]", new Object[]{url, timeOut, e});
            logger.info("Connection Pool Stats:: [{}]", poolingClientConnectionManager.getTotalStats());


        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
        return null;

    }

    public static String putData(String url, String data) throws Exception {
        return putData(url, data, false);
    }

    public static String putData(String url, String data, boolean useProxy) throws Exception {
        Map<String, String> headers = Collections.emptyMap();
        return putData(url, data, headers, useProxy);
    }

    public static String putData(String url, String data, Map<String, String> headers) throws Exception {
        return putData(url, data, headers, false);
    }

    public static String putData(String url, String data, Map<String, String> headers, boolean useProxy) throws Exception {
        return putData(url, data, headers, useProxy, SOCKET_TIMEOUT_MILLIS);
    }

    public static String putData(String url, String data, Map<String, String> headers, boolean useProxy, int timeout) throws Exception {
        HttpPut post = null;
        try {
            post = new HttpPut(url);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                post.setHeader(header.getKey(), header.getValue());
            }
            ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
            HttpEntity entity = new InputStreamEntity(bis, bis.available());
            post.setEntity(entity);
            HttpResponse response = getHttpClient(timeout, useProxy).execute(post);
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() >= 400) {
                throw new Exception(String.format("Got Http error code [%s]", statusLine.getStatusCode()));
            } else {
                String responseString = IOUtil.readData(response.getEntity().getContent(), true);
                logger.info("Got status code [{}] for url [{}]", statusLine.getStatusCode(), url);
                return responseString;
            }
        } catch (Exception e) {
            logger.error("Error requesting url [{}] with timeout [{}]", new Object[]{url, SOCKET_TIMEOUT_MILLIS, e});

            throw e;
        } finally {
            if (post != null)
                post.releaseConnection();
        }
    }


}
