package com.dev.raja.filters;

import io.swagger.core.filter.SwaggerSpecFilter;
import io.swagger.model.ApiDescription;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

import java.util.List;
import java.util.Map;

/**
 * Created by Raja on 03/03/17.
 */
public class CustomSwaggerSpecFilter implements io.swagger.core.filter.SwaggerSpecFilter {

    public static final String IGNORE_PARAM_NAME = "ignore_param";
    public static final String IGNORE_PARAM_ACCESS = "private";

    @Override
    public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return true;
    }

    @Override
    public boolean isParamAllowed(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        if (parameter != null &&
                (IGNORE_PARAM_NAME.equalsIgnoreCase(parameter.getName())
                        || IGNORE_PARAM_ACCESS.equalsIgnoreCase(parameter.getAccess()))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isPropertyAllowed(Model model, Property property, String propertyName, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers) {
        return true;
    }
}
