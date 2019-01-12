package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.FrontendConstants;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.meta.UserAwareMeta;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.operation.services.OperationExecutor;
import com.developmentontheedge.be5.operation.services.OperationService;
import com.developmentontheedge.be5.operation.util.Either;
import com.developmentontheedge.be5.security.UserInfoProvider;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.OperationResultPresentation;
import com.developmentontheedge.be5.server.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.services.OperationLogging;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.LayoutUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;

import static com.developmentontheedge.be5.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class FormGeneratorImpl implements FormGenerator
{
    private final UserAwareMeta userAwareMeta;
    private final OperationService operationService;
    private final OperationExecutor operationExecutor;
    private final UserInfoProvider userInfoProvider;
    private final ErrorModelHelper errorModelHelper;
    private final OperationLogging operationLogging;

    @Inject
    public FormGeneratorImpl(
            UserAwareMeta userAwareMeta,
            OperationService operationService,
            OperationExecutor operationExecutor,
            UserInfoProvider userInfoProvider,
            ErrorModelHelper errorModelHelper,
            OperationLogging operationLogging)
    {
        this.userAwareMeta = userAwareMeta;
        this.operationService = operationService;
        this.operationExecutor = operationExecutor;
        this.userInfoProvider = userInfoProvider;
        this.errorModelHelper = errorModelHelper;
        this.operationLogging = operationLogging;
    }

    @Override
    public ResourceData generate(String entityName, String queryName, String operationName,
                                 Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, operationParams);
        return generate(operation, values);
    }

    @Override
    public ResourceData execute(String entityName, String queryName, String operationName,
                                Map<String, Object> operationParams, Map<String, Object> values)
    {
        Operation operation = getOperation(entityName, queryName, operationName, operationParams);
        return execute(operation, values);
    }

    private Operation getOperation(String entityName, String queryName, String operationName, Map<String, Object> operationParams)
    {
        OperationInfo operationInfo = new OperationInfo(userAwareMeta.getOperation(entityName, queryName, operationName));
        return operationExecutor.create(operationInfo, queryName, operationParams);
    }

    @LogBe5Event
    ResourceData generate(Operation operation, Map<String, Object> values)
    {
        Either<Object, OperationResult> resultEither = operationService.generate(operation, values);
        operationLogging.saveOperationLog(operation, values);
        return getResult(operation, resultEither);
    }

    @LogBe5Event
    ResourceData execute(Operation operation, Map<String, Object> values)
    {
        Either<Object, OperationResult> resultEither = operationService.execute(operation, values);
        operationLogging.saveOperationLog(operation, values);
        return getResult(operation, resultEither);
    }

    private ResourceData getResult(Operation operation,
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
            Map<String, Object> layout = LayoutUtils.getLayoutObject(operation.getInfo().getModel());
            layout.remove("type");
            data = Either.second(new OperationResultPresentation(
                    resultForFrontend(result.getSecond()),
                    layout
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
        Be5Exception be5Exception = e.getClass() == Be5Exception.class ? (Be5Exception) e : Be5Exception.internal(e);
        return errorModelHelper.getErrorModel(be5Exception,
                Collections.singletonMap(SELF_LINK, url.toString()));
    }

    private static HashUrl getUrl(Operation operation)
    {
        return new HashUrl(FrontendConstants.FORM_ACTION,
                operation.getInfo().getEntityName(), operation.getContext().getQueryName(), operation.getInfo().getName())
                .named(operation.getRedirectParams());
    }
}
