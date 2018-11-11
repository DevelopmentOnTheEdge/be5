package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.FrontendConstants;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
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
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.OperationResultPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.beans.json.JsonFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class FormGeneratorImpl implements FormGenerator
{
    private static final Logger log = Logger.getLogger(FormGeneratorImpl.class.getName());

    private final UserAwareMeta userAwareMeta;
    private final OperationService operationService;
    private final OperationExecutor operationExecutor;
    private final UserInfoProvider userInfoProvider;
    private final ErrorModelHelper errorModelHelper;

    @Inject
    public FormGeneratorImpl(
            UserAwareMeta userAwareMeta,
            OperationService operationService,
            OperationExecutor operationExecutor,
            UserInfoProvider userInfoProvider,
            ErrorModelHelper errorModelHelper)
    {
        this.userAwareMeta = userAwareMeta;
        this.operationService = operationService;
        this.operationExecutor = operationExecutor;
        this.userInfoProvider = userInfoProvider;
        this.errorModelHelper = errorModelHelper;
    }

    @Override
    public ResourceData generate(String entityName, String queryName, String operationName,
                                 Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, operationParams);
        Either<Object, OperationResult> result = operationService.generate(operation, values);
        return getResult(operation, result);
    }

    @Override
    public ResourceData execute(String entityName, String queryName, String operationName,
                                Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, operationParams);
        Either<Object, OperationResult> result = operationService.execute(operation, values);
        return getResult(operation, result);
    }

    private Operation getOperation(String entityName, String queryName, String operationName, Map<String, Object> operationParams)
    {
        Operation operation;

        OperationInfo operationInfo = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
        operation = operationExecutor.create(operationInfo, queryName, operationParams);
        return operation;
    }

    private ResourceData getResult(com.developmentontheedge.be5.operation.model.Operation operation,
                                   Either<Object, OperationResult> result)
    {
        Either<FormPresentation, OperationResultPresentation> data;
        if (result.isFirst())
        {
            String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(operation.getInfo().getEntity());
            String localizedOperationTitle = userAwareMeta.getLocalizedOperationTitle(operation.getInfo().getModel());
            String title = localizedEntityTitle + ": " + localizedOperationTitle;

            data = Either.first(new FormPresentation(
                    operation.getInfo(),
                    operation.getContext(),
                    title,
                    JsonFactory.bean(result.getFirst()),
                    LayoutUtils.getLayoutObject(operation.getInfo().getModel()),
                    resultForFrontend(operation.getResult()),
                    getErrorModel(operation)
            ));
        }
        else
        {
            data = Either.second(new OperationResultPresentation(
                    resultForFrontend(result.getSecond()),
                    LayoutUtils.getLayoutObject(operation.getInfo().getModel())
            ));
        }
        return new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
                Collections.singletonMap(SELF_LINK, getUrl(operation).toString()));
    }

    private ErrorModel getErrorModel(Operation operation)
    {
        if (userInfoProvider.isSystemDeveloper() && operation.getResult().getStatus() == OperationStatus.ERROR)
        {
            return getErrorModel((Throwable) operation.getResult().getDetails(), getUrl(operation));
        }
        return null;
    }

    private OperationResult resultForFrontend(OperationResult result)
    {
        if (result.getStatus() == OperationStatus.ERROR)
        {
            return OperationResult.error(userAwareMeta.getLocalizedExceptionMessage(result.getMessage()), null);
        }
        else
        {
            return result;
        }
    }

    private ErrorModel getErrorModel(Throwable e, HashUrl url)
    {
        log.log(Level.SEVERE, "Error in operation: " + url.toString(), e);
        return errorModelHelper.getErrorModel(Be5Exception.internal(e),
                Collections.singletonMap(SELF_LINK, url.toString()));
    }

    private static HashUrl getUrl(Operation operation)
    {
        return new HashUrl(FrontendConstants.FORM_ACTION,
                operation.getInfo().getEntityName(), operation.getContext().getQueryName(), operation.getInfo().getName())
                .named(operation.getRedirectParams());
    }
}
