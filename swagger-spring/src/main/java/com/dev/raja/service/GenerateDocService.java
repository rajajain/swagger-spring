package com.dev.raja.service;

import com.dev.raja.filters.CustomSwaggerSpecFilter;
import com.dev.raja.http.HttpClient;
import com.dev.raja.http.IOUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.models.Swagger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * Created by raja on 03/03/17.
 */
@Service
public class GenerateDocService implements ApplicationContextAware {

    public static final String BACKSLASH = "/";
    public static final String API_DOC_ATTRIBUTE_PROJECT_NAME = "project_name";
    public static final String API_DOC_ATTRIBUTE_VERSION = "version";
    public static final String API_DOC_ATTRIBUTE_API_DOC = "api_doc";
    public static final String API_DOC_ATTRIBUTE_CREATION_TIME = "creation_time";

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void generateDocAndWriteToFile(String fileLocation, String fileName, String projectName, String version) {
        Swagger doc = getSwaggerDoc();
        Map<String, Object> nodes = new HashMap<>();
        nodes.put(API_DOC_ATTRIBUTE_PROJECT_NAME, projectName);
        nodes.put(API_DOC_ATTRIBUTE_VERSION, version);
        nodes.put(API_DOC_ATTRIBUTE_CREATION_TIME, System.currentTimeMillis());
        nodes.put(API_DOC_ATTRIBUTE_API_DOC, doc);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            IOUtil.writeToFile(fileLocation + BACKSLASH + projectName + BACKSLASH + version, fileName,
                    objectMapper.writeValueAsString(nodes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public void generateDocAndShipOverHttp(String url, Map<String, String> urlParams, String projectName, String version) {
        Swagger doc = getSwaggerDoc();
        Map<String, Object> nodes = new HashMap<>();
        nodes.put(API_DOC_ATTRIBUTE_PROJECT_NAME, projectName);
        nodes.put(API_DOC_ATTRIBUTE_VERSION, version);
        nodes.put(API_DOC_ATTRIBUTE_CREATION_TIME, System.currentTimeMillis());
        nodes.put(API_DOC_ATTRIBUTE_API_DOC, doc);
        try {
            StringBuilder urlBuilder = new StringBuilder(url);
            if (!CollectionUtils.isEmpty(urlParams)) {
                urlBuilder.append("?");
                for (Map.Entry<String, String> value : urlParams.entrySet()) {
                    urlBuilder.append(value.getKey()).append("=").append(value.getValue()).append("&");
                }
                urlBuilder.deleteCharAt(urlBuilder.length() - 1);
            } else {

            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            HttpClient.postData(urlBuilder.toString(), objectMapper.writeValueAsString(nodes), Boolean.valueOf(System.getProperty("doc.gen.use.proxy")));
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public void generateDocByIdAndShipOverHttp(String url, Map<String, String> urlParams, String id, String projectName, String version) {
        Swagger doc = getSwaggerDoc();
        Map<String, Object> nodes = new HashMap<>();
        nodes.put(API_DOC_ATTRIBUTE_PROJECT_NAME, projectName);
        nodes.put(API_DOC_ATTRIBUTE_VERSION, version);
        nodes.put(API_DOC_ATTRIBUTE_CREATION_TIME, System.currentTimeMillis());
        nodes.put(API_DOC_ATTRIBUTE_API_DOC, doc);
        try {
            StringBuilder urlBuilder = new StringBuilder(url);
            urlBuilder.append("?id=").append(id);
            if (!CollectionUtils.isEmpty(urlParams)) {
                for (Map.Entry<String, String> value : urlParams.entrySet()) {
                    urlBuilder.append("&").append(value.getKey()).append("=").append(value.getValue());
                }
            }
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            HttpClient.putData(urlBuilder.toString(), objectMapper.writeValueAsString(nodes), Boolean.valueOf(System.getProperty("doc.gen.use.proxy")));
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public Swagger getSwaggerDoc() {
        Map<String, Object> controllerBeanMap = applicationContext.getBeansWithAnnotation(Controller.class);
        Assert.isTrue(!CollectionUtils.isEmpty(controllerBeanMap));
        Collection<Object> controllerBeans = controllerBeanMap.values();
        Assert.isTrue(!CollectionUtils.isEmpty(controllerBeans));
        Set controllers = new HashSet();
        for (Object controllerBean : controllerBeans) {
            Class controller = controllerBean.getClass();
            controllers.add(controller);
        }

        SwaggerSpecFilter swaggerSpecFilter = new CustomSwaggerSpecFilter();
        final Swagger swagger = new Swagger();
        io.swagger.servlet.Reader.read(swagger, Collections.<Class<?>>unmodifiableSet(controllers));


        final Swagger filtered = new io.swagger.core.filter.SpecFilter().filter(swagger, swaggerSpecFilter, null, null, null);
        return filtered;
    }

}
