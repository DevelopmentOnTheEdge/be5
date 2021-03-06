package com.developmentontheedge.be5.server.controllers;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.server.model.OperationResultPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.ErrorModelHelper;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.servlet.support.JsonApiModelController;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.server.RestApiConstants.CONTEXT_PARAMS;
import static com.developmentontheedge.be5.server.RestApiConstants.ENTITY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.OPERATION_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.QUERY_NAME_PARAM;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.server.RestApiConstants.TIMESTAMP_PARAM;
import static java.util.Objects.requireNonNull;

@Singleton
public class FormController extends JsonApiModelController
{
    private static final Logger log = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());

    private final FormGenerator formGenerator;
    private final ErrorModelHelper errorModelHelper;

    @Inject
    public FormController(FormGenerator formGenerator, ErrorModelHelper errorModelHelper)
    {
        this.formGenerator = formGenerator;
        this.errorModelHelper = errorModelHelper;
    }

    @Override
    public JsonApiModel generateJson(Request req, Response res, String requestSubUrl)
    {
        requireNonNull(req.get(TIMESTAMP_PARAM));
        String entityName = req.getNonEmpty(ENTITY_NAME_PARAM);
        String queryName = req.getNonEmpty(QUERY_NAME_PARAM);
        String operationName = req.getNonEmpty(OPERATION_NAME_PARAM);
        Map<String, Object> contextParams = ParseRequestUtils.getContextParams(req.getNonEmpty(CONTEXT_PARAMS));
        Map<String, Object> values = ParseRequestUtils.getFormValues(req.getParameters());

        try
        {
            switch (requestSubUrl)
            {
                case "":
                    ResourceData generateData = formGenerator.generate(entityName, queryName,
                            operationName, contextParams, values);
                    if (FrontendConstants.OPERATION_RESULT.equals(generateData.getType()) &&
                            ((OperationResultPresentation) generateData.getAttributes()).getOperationResult()
                                    .getStatus() == OperationStatus.ERROR)
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return data(generateData);
                case "apply":
                    ResourceData executeData = formGenerator.execute(entityName, queryName,
                            operationName, contextParams, values);
                    if (FORM_ACTION.equals(executeData.getType()))
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    else if (((OperationResultPresentation) executeData.getAttributes()).
                            getOperationResult().getStatus() == OperationStatus.ERROR)
                    {
                        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                    return data(executeData);
                default:
                    return null;
            }
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName)
                    .named(contextParams);
            log.log(e.getLogLevel(), "Error in operation: " + url +
                    ", on requestSubUrl = '" + requestSubUrl + "'", e);
            return error(errorModelHelper.getErrorModel(e, Collections.singletonMap(SELF_LINK, url.toString())));
        }
    }
}
