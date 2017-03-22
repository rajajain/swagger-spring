package com.dev.raja.handler;

import io.swagger.annotations.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by raja on 04/03/17.
 */
@SwaggerDefinition(
        info = @Info(
                description = "Sample API Doc",
                version = "1.0.0",
                title = "Sample API Doc",
                termsOfService = "link_to_terms",
                contact = @Contact(name = "Raja", url = "site"),
                license = @License(name = "License", url = "license_url")
        ),
        basePath = "/",
        host = "test.com",
        consumes = {"application/json", "application/xml", ""},
        produces = {"application/json", "application/xml", ""},
        schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS, SwaggerDefinition.Scheme.DEFAULT},
        externalDocs = @ExternalDocs(value = "docs", url = "url_to_docs")
)
@Api(
        value = "/v1/sample",
        consumes = "application/json, application/xml",
        produces = "application/json, application/xml",
        protocols = "http",
        tags = {"sample_controller"}
)
@Controller(value = "/v1/sample.*")
public class SampleController {

    @ApiOperation(
            value = "Login api",
            notes = "Use this method to Login.",
            httpMethod = "GET",
            nickname = "login",
            protocols = "http",
            tags = {"Login"},
            response = String.class
    )
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_name", value = "username", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "password", value = "password", dataType = "string", paramType = "query")
    })
    @org.springframework.web.bind.annotation.RequestMapping(value = "/v1/sample/login",
            method = RequestMethod.GET, produces = "application/json")
    public String loginClient(@ApiParam(name = "ignore_param") String requestUri) {
        return "OK";
    }
}
