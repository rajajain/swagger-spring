package com.dev.raja;

import com.dev.raja.service.GenerateDocWrapper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by raja on 03/03/17.
 */
@SpringBootApplication()
public class EntryPoint {

    public static void main(String[] args) {
        ApplicationContext applicationContext =
                new SpringApplicationBuilder(EntryPoint.class).web(false).run(args);
        GenerateDocWrapper wrapper = applicationContext.getBean(GenerateDocWrapper.class);
        wrapper.generateDocWriteToFile("/tmp", "swagger.json", "sample_project", "V_10");
        Map<String, String> map = new HashMap<>();
        map.put("index", "knowledge_base");
        map.put("type", "swagger_api_doc");
        wrapper.generateDocByIdAndShipOverHttp("http://test/v1/test", map, "sample_project_v_10", "sample_project", "v_10");
        wrapper.generateDocAndShipOverHttp("http://test/v1/test", map, "sample_project", "v_10");

    }
}
