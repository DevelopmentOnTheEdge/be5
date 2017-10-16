package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.impl.OperationServiceImpl;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.OperationInfo;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;

import java.util.Collections;
import java.util.logging.Logger;

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
        OperationService operationService = injector.get(OperationService.class);
        UserAwareMeta userAwareMeta = injector.get(UserAwareMeta.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
//        Map<String, Object> presetValues = req.getValuesFromJson(RestApiConstants.VALUES);
        OperationInfo meta = userAwareMeta.getOperation(entityName, queryName, operationName);
        String link = (String) new OperationInfo(queryName, meta.getModel())
                .redirectThisOperation(OperationServiceImpl.selectedRows(selectedRowsString))
                .getDetails();
        Either<FormPresentation, OperationResult> generate;

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    generate = operationService.generate(req);
                    break;
                case "apply":
                    generate = operationService.execute(req);
                    break;
                default:
                    res.sendUnknownActionError();
                    return;
            }
        }
        catch (Be5Exception e)
        {
            res.sendErrorAsJson(
                    new ErrorModel("500", e.getTitle(), Be5Exception.getMessage(e), Be5Exception.exceptionAsString(e)),
                    Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                    Collections.singletonMap(SELF_LINK, link)
            );
            return;
        }
        res.sendAsJson(
                new ResourceData(generate.isFirst() ? FORM_ACTION : OPERATION_RESULT, generate.get()),
                Collections.singletonMap(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM)),
                Collections.singletonMap(SELF_LINK, link)
        );
    }

}
