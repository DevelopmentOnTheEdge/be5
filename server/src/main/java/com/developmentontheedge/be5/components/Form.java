package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.ParseRequestUtils;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.components.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.components.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.google.common.base.Strings.nullToEmpty;


public class Form implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        OperationExecutor operationExecutor = injector.get(OperationExecutor.class);
        DocumentGenerator documentGenerator = injector.get(DocumentGenerator.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String[] selectedRows = ParseRequestUtils.selectedRows(nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS)));
        Map<String, String> operationParams = req.getValuesFromJsonAsStrings(RestApiConstants.OPERATION_PARAMS);
        Map<String, Object> values = req.getValuesFromJson(RestApiConstants.VALUES);

        Operation operation;
        try
        {
            operation = operationExecutor.create(entityName, queryName, operationName, selectedRows, operationParams);
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName).named(operationParams);
            res.sendErrorAsJson(
                    new ErrorModel(e, "", Collections.singletonMap(SELF_LINK, url.toString())),
                    req.getDefaultMeta()
            );
            return;
        }

        Either<FormPresentation, OperationResult> data;

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    data = documentGenerator.generateForm(operation, values);
                    break;
                case "apply":
                    data = documentGenerator.executeForm(operation, values);
                    break;
                default:
                    res.sendUnknownActionError();
                    return;
            }
        }
        catch (Be5Exception e)
        {
            res.sendErrorAsJson(
                    documentGenerator.getErrorModel(e, operation.getUrl()),
                    req.getDefaultMeta()
            );
            return;
        }

        res.sendAsJson(
                new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
                        Collections.singletonMap(SELF_LINK, operation.getUrl().toString())),
                req.getDefaultMeta()
        );
    }

}
