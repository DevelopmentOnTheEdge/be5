package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.components.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.components.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;
import static com.google.common.base.Strings.nullToEmpty;


public class Form implements Component
{
    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        OperationExecutor operationExecutor = injector.get(OperationExecutor.class);
        OperationService operationService = injector.get(OperationService.class);
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String[] selectedRows = ParseRequestUtils.selectedRows(nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS)));
        Map<String, String> operationParams = req.getValuesFromJsonAsStrings(RestApiConstants.OPERATION_PARAMS);
        Map<String, Object> values = req.getValuesFromJson(RestApiConstants.VALUES);

        OperationInfo operationInfo;
        try
        {
            operationInfo = userAwareMeta.getOperation(entityName, operationName);
        }
        catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName).named(operationParams);
            sendError(req, res, url, e);
            return;
        }

        OperationContext operationContext = new OperationContext(selectedRows, queryName, operationParams);

        Operation operation = operationExecutor.create(operationInfo, operationContext);

        Either<Object, OperationResult> result;

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    result = operationService.generate(operation, values);
                    break;
                case "apply":
                    result = operationService.execute(operation, values);
                    break;
                default:
                    res.sendUnknownActionError();
                    return;
            }
        }
        catch (Be5Exception e)
        {
            //todo remove this block, catch in operationService
            res.sendErrorAsJson(
                    getErrorModel(e, injector),
                    Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                    Collections.singletonMap(SELF_LINK, operation.getUrl().toString())
            );
            return;
        }

        Object data;
        if(result.isFirst())
        {
            ErrorModel errorModel = null;
            if(operation.getResult().getStatus() == OperationStatus.ERROR)
            {
                if(UserInfoHolder.isSystemDeveloper())
                {
                    errorModel = getErrorModel((Throwable) operation.getResult().getDetails(), injector);
                }

                //todo refactoring, add for prevent json error
                //java.lang.IllegalAccessException: Class org.eclipse.yasson.internal.model.GetFromGetter can not access a member of class sun.reflect.annotation.AnnotatedTypeFactory$AnnotatedTypeBaseImpl with modifiers "public final"
                //at sun.reflect.Reflection.ensureMemberAccess(Reflection.java:102)
                operation.setResult(OperationResult.error(operation.getResult().getMessage().split(System.getProperty("line.separator"))[0]));
            }

            data = new FormPresentation(
                    operationInfo,
                    operationContext,
                    userAwareMeta.getLocalizedOperationTitle(operationInfo),
                    JsonFactory.bean(result.getFirst()),
                    operation.getLayout(),
                    operation.getResult(),
                    errorModel
            );
        }
        else
        {
            data = result.getSecond();
        }

        res.sendAsJson(
                new ResourceData(result.isFirst() ? FORM_ACTION : OPERATION_RESULT, data),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, operation.getUrl().toString())
        );
    }

    //todo refactoring
    private void sendError(Request req, Response res, HashUrl url, Be5Exception e)
    {
        String message = "";

        //message += GroovyRegister.getErrorCodeLine(e, query.getQuery());

        res.sendErrorAsJson(
                new ErrorModel(e, message),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, url.toString())
        );
    }

    private ErrorModel getErrorModel(Throwable e, Injector injector)
    {
        String message = Be5Exception.getMessage(e);

        if(UserInfoHolder.isSystemDeveloper())message += injector.get(GroovyRegister.class).getErrorCodeLine(e);

        return new ErrorModel("500", e.getMessage(), message, Be5Exception.exceptionAsString(e));
    }

}
