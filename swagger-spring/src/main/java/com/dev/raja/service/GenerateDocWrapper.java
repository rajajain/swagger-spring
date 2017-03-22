package com.dev.raja.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Properties;

/**
 * Created by raja on 04/03/17.
 */
public class GenerateDocWrapper {

    @Autowired
    private GenerateDocService generateDocService;

    public GenerateDocWrapper(Properties properties) {
        for (Map.Entry prop : properties.entrySet()) {
            System.setProperty(String.valueOf(prop.getKey()), String.valueOf(prop.getValue()));
        }
    }

    /**
     * Method to generate api doc and write to a file
     *
     * @param fileLocation Directory path where fileName will be created
     * @param fileName     Name of generated doc file
     * @param projectName  Name of project to identify/group the docs per project.
     * @param version      Version number to keep track of api doc changes.
     */
    public void generateDocWriteToFile(String fileLocation, String fileName, String projectName, String version) {
        generateDocService.generateDocAndWriteToFile(fileLocation, fileName, projectName, version);
    }

    /**
     * Method to generate api doc and ship over http.
     *
     * @param url         {@link org.apache.http.protocol.HTTP} URL , the target location, where doc to be shipped
     * @param urlParams   params to be passed in url
     * @param projectName Name of project to identify/group the docs per project.
     * @param version     Version number to keep track of api doc changes.
     */
    public void generateDocAndShipOverHttp(String url, Map<String, String> urlParams, String projectName, String version) {
        generateDocService.generateDocAndShipOverHttp(url, urlParams, projectName, version);
    }

    /**
     * Method to generate api doc and ship over http.
     *
     * @param url         {@link org.apache.http.protocol.HTTP} URL , the target location, where doc to be shipped
     * @param urlParams   params to be passed in url
     * @param id          ID of api doc can be projectName+ "_" +version , but should be consistent per version.
     * @param projectName Name of project to identify/group the docs per project.
     * @param version     Version number to keep track of api doc changes.
     */
    public void generateDocByIdAndShipOverHttp(String url, Map<String, String> urlParams, String id, String projectName, String version) {
        generateDocService.generateDocByIdAndShipOverHttp(url, urlParams, id, projectName, version);
    }


}
