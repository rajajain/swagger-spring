package com.dev.raja.config;

import com.dev.raja.http.HttpClient;
import com.dev.raja.service.GenerateDocWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * Created by raja on 04/03/17.
 */
@Configuration
public class EnvironmentConfig {

    @Bean(name = "generateDocWrapper")
    public com.dev.raja.service.GenerateDocWrapper generateDocWrapper() {
        Properties props = new Properties();
        props.put(HttpClient.USE_PROXY, Boolean.TRUE);
        props.put(HttpClient.HTTP_PROXY_HOST, "test");
        props.put(HttpClient.HTTP_PROXY_PORT, "8080");
        props.put(HttpClient.IS_HTTP_PROXY_SECURE, Boolean.FALSE);
        props.put(HttpClient.HTTP_PROXY_AUTH_ENABLED, Boolean.TRUE);
        props.put(HttpClient.HTTP_PROXY_AUTH_NAME, "raja");
        props.put(HttpClient.HTTP_PROXY_AUTH_PASSWORD, "password");

        return new GenerateDocWrapper(props);
    }
}
