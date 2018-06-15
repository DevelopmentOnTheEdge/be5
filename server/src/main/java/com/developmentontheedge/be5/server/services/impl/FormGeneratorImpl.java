package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationInfo;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.operation.model.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.server.helpers.JsonApiResponseHelper;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.util.HashUrlUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class FormGeneratorImpl implements FormGenerator
{
    private static final Logger log = Logger.getLogger(FormGeneratorImpl.class.getName());

    private final UserAwareMeta userAwareMeta;
    private final GroovyRegister groovyRegister;
    private final OperationService operationService;
    private final OperationExecutor operationExecutor;
    private final UserInfoProvider userInfoProvider;
    private final JsonApiResponseHelper responseHelper;

    @Inject
    public FormGeneratorImpl(
            UserAwareMeta userAwareMeta,
            GroovyRegister groovyRegister,
            OperationService operationService,
            OperationExecutor operationExecutor,
            UserInfoProvider userInfoProvider,
            JsonApiResponseHelper responseHelper)
    {
        this.userAwareMeta = userAwareMeta;
        this.groovyRegister = groovyRegister;
        this.operationService = operationService;
        this.operationExecutor = operationExecutor;
        this.userInfoProvider = userInfoProvider;
        this.responseHelper = responseHelper;
    }

    @Override
    public ResourceData generate(String entityName, String queryName, String operationName,
                                 String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, selectedRows, operationParams);

        Either<FormPresentation, OperationResult> data = processForm(operation, values, false);

        return new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
                Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString()));
    }

    @Override
    public ResourceData execute(String entityName, String queryName, String operationName,
                                String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, selectedRows, operationParams);

        Either<FormPresentation, OperationResult> data = processForm(operation, values, true);

        return new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
                Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString()));
    }

    private Operation getOperation(String entityName, String queryName, String operationName, String[] selectedRows, Map<String, Object> operationParams)
    {
        Operation operation;

        OperationInfo operationInfo = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
        operation = operationExecutor.create(operationInfo, queryName, selectedRows, operationParams);
        return operation;
    }

//    @Override
//    public JsonApiModel getJsonApiModel(String method, String entityName, String queryName, String operationName,
//                                        String[] selectedRows, Map<String, Object> operationParams, Map<String, Object> values)
//    {
//        HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName).named(operationParams);
//
//        com.developmentontheedge.be5.operation.model.Operation operation;
//
//        try
//        {
//            OperationInfo operationInfo = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
//            operation = operationExecutor.create(operationInfo, queryName, selectedRows, operationParams);
//        }
//        catch (Be5Exception e)
//        {
//            log.log(Level.SEVERE, "Error on create operation: " + url.toString(), e);
//
//            return JsonApiModel.error(
//                    responseHelper.getErrorModel(e, "", Collections.singletonMap(SELF_LINK, url.toString())),
//                    null);
//        }
//
//        try
//        {
//            switch (method)
//            {
//                case "":
//                    Either<FormPresentation, OperationResult> data = generate(operation, values);
//                    return JsonApiModel.data(new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
//                            Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString())), null);
//                case "apply":
//                    Either<FormPresentation, OperationResult> applyData = execute(operation, values);
//                    return JsonApiModel.data(new ResourceData(applyData.isFirst() ? FORM_ACTION : OPERATION_RESULT, applyData.get(),
//                            Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString())), null);
//                default:
//                    return JsonApiModel.error(new ErrorModel("404", "Unknown component action."), null);
//            }
//        }
//        catch (Be5Exception e)
//        {
//            HashUrl url2 = HashUrlUtils.getUrl(operation);
//            log.log(Level.SEVERE, "Error in operation: " + url2.toString(), e);
//
//            return JsonApiModel.error(
//                    getErrorModel(e, url2),
//                    null);
//        }
//    }

//    @Override
//    public Either<FormPresentation, OperationResult> generate(com.developmentontheedge.be5.operation.model.Operation operation,
//                                                              Map<String, ?> values)
//    {
//        return processForm(operation, values, false);
//    }
//
//    @Override
//    public Either<FormPresentation, OperationResult> execute(com.developmentontheedge.be5.operation.model.Operation operation,
//                                                             Map<String, ?> values)
//    {
//        return processForm(operation, values, true);
//    }

    private Either<FormPresentation, OperationResult> processForm(com.developmentontheedge.be5.operation.model.Operation operation,
                                                                  Map<String, ?> values, boolean execute)
    {
        Either<Object, OperationResult> result;
        if(execute)
        {
            result = operationService.execute(operation, (Map<String, Object>)values);
        }
        else
        {
            result = operationService.generate(operation, (Map<String, Object>)values);
        }

        if(result.isFirst())
        {
            ErrorModel errorModel = null;
            if(operation.getResult().getStatus() == OperationStatus.ERROR)
            {
                if (userInfoProvider.isSystemDeveloper())
                {
                    errorModel = getErrorModel((Throwable) operation.getResult().getDetails(), HashUrlUtils.getUrl(operation));
                }
            }

            return Either.first(new FormPresentation(
                    operation.getInfo(),
                    operation.getContext(),
                    userAwareMeta.getLocalizedOperationTitle(operation.getInfo().getModel()),
                    JsonFactory.bean(result.getFirst()),
                    LayoutUtils.getLayoutObject(operation.getInfo().getModel()),
                    resultForFrontend(operation.getResult()),
                    errorModel
            ));
        }
        else
        {
            return Either.second(resultForFrontend(result.getSecond()));
        }
    }

    private OperationResult resultForFrontend(OperationResult result)
    {
        if(result.getStatus() == OperationStatus.ERROR)
        {
            return OperationResult.error(userAwareMeta.getLocalizedExceptionMessage(result.getMessage()));
        }
        else
        {
            return result;
        }
    }

    //@Override
    public ErrorModel getErrorModel(Throwable e, HashUrl url)
    {
        return responseHelper.getErrorModel(Be5Exception.internal(e),
                Collections.singletonMap(SELF_LINK, url.toString()));
    }

}
