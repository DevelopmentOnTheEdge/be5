package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.components.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.components.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;
import static com.google.common.base.Strings.nullToEmpty;


public class Form implements Component
{
    private static final Logger log = Logger.getLogger(Form.class.getName());

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        OperationExecutor operationExecutor = injector.get(OperationExecutor.class);
        OperationService operationService = injector.get(OperationService.class);
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String[] selectedRows = JsonUtils.selectedRows(nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS)));
        Map<String, Object> presetValues = req.getValuesFromJson(RestApiConstants.VALUES);

        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);

        String link = (String) new OperationInfo(queryName, meta.getModel())
                .redirectThisOperation(selectedRows, Collections.emptyMap())
                .getDetails();

        Operation operation = operationExecutor.create(meta, selectedRows);
        Either<Object, OperationResult> generate;

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    generate = operationService.generate(operation, presetValues);
                    break;
                case "apply":
                    generate = operationService.execute(operation, presetValues);
                    break;
                default:
                    res.sendUnknownActionError();
                    return;
            }
        }
        catch (Be5Exception e)
        {
            String message = Be5Exception.getMessage(e);

            message += getErrorCodeLine(e, meta.getCode());

            res.sendErrorAsJson(
                    new ErrorModel("500", e.getTitle(), message, Be5Exception.exceptionAsString(e)),
                    Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                    Collections.singletonMap(SELF_LINK, link)
            );
            return;
        }

        Object result;
        if(generate.isFirst())
        {
            result = new FormPresentation(operation.getInfo(),
                    userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                    Arrays.stream(operation.getRecords()).collect(Collectors.joining(",")),
                    JsonFactory.bean(generate.getFirst()), operation.getLayout(),
                    operation.getResult());
        }
        else
        {
            result = generate.getSecond();
        }

        res.sendAsJson(
                new ResourceData(generate.isFirst() ? FORM_ACTION : OPERATION_RESULT, result),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, link)
        );
    }

    private static String getErrorCodeLine(Throwable e, String code)
    {
        //String lines[] = code.split("\\r?\\n");

        int id = 0;
        if(e.getStackTrace()[0].getClassName().equals("org.codehaus.groovy.runtime.BytecodeInterface8"))
        {
            id = 1;
        }
        return "\n" + e.getStackTrace()[id].getClassName() + "." + e.getStackTrace()[id].getMethodName()
                + "(" + e.getStackTrace()[id].getFileName() + ":" + e.getStackTrace()[id].getLineNumber() + ")";
    }

}
