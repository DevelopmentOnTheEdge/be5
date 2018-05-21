package com.developmentontheedge.be5.controllers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.RestApiConstants;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.api.support.ControllerSupport;
import com.developmentontheedge.be5.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.HashUrlUtils;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import javax.inject.Inject;
import com.google.inject.Stage;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.api.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;
import static com.google.common.base.Strings.nullToEmpty;


public class FormController extends ControllerSupport
{
    private static final Logger log = Logger.getLogger(FormController.class.getName());

    private final OperationExecutor operationExecutor;
    private final DocumentGenerator documentGenerator;
    private final UserHelper userHelper;
    private final UserAwareMeta userAwareMeta;
    private final Stage stage;

    @Inject
    public FormController(OperationExecutor operationExecutor, DocumentGenerator documentGenerator,
                          UserHelper userHelper, UserAwareMeta userAwareMeta, Stage stage)
    {
        this.operationExecutor = operationExecutor;
        this.documentGenerator = documentGenerator;
        this.userHelper = userHelper;
        this.userAwareMeta = userAwareMeta;
        this.stage = stage;
    }

    @Override
    public void generate(Request req, Response res)
    {
        if(stage == Stage.DEVELOPMENT && UserInfoHolder.getUserInfo() == null)
        {
            userHelper.initGuest(req);
        }

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String[] selectedRows = ParseRequestUtils.selectedRows(nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS)));
        Map<String, Object> operationParams = req.getValuesFromJson(RestApiConstants.OPERATION_PARAMS);
        Map<String, Object> values = req.getValuesFromJson(RestApiConstants.VALUES);

        HashUrl url = new HashUrl(FORM_ACTION, entityName, queryName, operationName).named(operationParams);

        com.developmentontheedge.be5.metadata.model.Operation operationMeta;
        Operation operation;
        try
        {
            operationMeta = userAwareMeta.getOperation(entityName, queryName, operationName);
            operation = operationExecutor.create(operationMeta, queryName, selectedRows, operationParams);
        }
        catch (Be5Exception e)
        {
            log.log(Level.SEVERE, "Error on create operation: " + url.toString(), e);
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
            HashUrl url2 = HashUrlUtils.getUrl(operation);
            log.log(Level.SEVERE, "Error in operation: " + url2.toString(), e);

            res.sendErrorAsJson(
                    documentGenerator.getErrorModel(e, url2),
                    req.getDefaultMeta()
            );
            return;
        }

        res.sendAsJson(
                new ResourceData(data.isFirst() ? FORM_ACTION : OPERATION_RESULT, data.get(),
                        Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString())),
                req.getDefaultMeta()
        );
    }

}
