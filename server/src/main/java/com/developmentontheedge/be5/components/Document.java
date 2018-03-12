package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.components.impl.MoreRowsGenerator;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationContext;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.ParseRequestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.components.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.components.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.components.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;


public class Document implements Component 
{
    private static final Logger log = Logger.getLogger(Document.class.getName());

    private DocumentGenerator documentGenerator;
    private UserAwareMeta userAwareMeta;
    private OperationExecutor operationExecutor;

    @Override
    public void generate(Request req, Response res, Injector injector)
    {
        documentGenerator = injector.get(DocumentGenerator.class);
        userAwareMeta = injector.get(UserAwareMeta.class);
        operationExecutor = injector.get(OperationExecutor.class);

        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        int sortColumn = req.getInt("order[0][column]", -1);
        boolean sortDesc = "desc".equals(req.get("order[0][dir]"));
        Map<String, String> parametersMap = req.getValuesFromJsonAsStrings(RestApiConstants.VALUES);

        HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parametersMap);

        Query query;
        try
        {
            query = userAwareMeta.getQuery(entityName, queryName);
        }
        catch (Be5Exception e)
        {
            sendError(req, res, url, e);
            return;
        }

        try
        {
            switch (req.getRequestUri())
            {
                case "":
                    sendQueryResponseData(req, res, url,
                            documentGenerator.routeAndRun(query, parametersMap, sortColumn, sortDesc), query);
                    return;
                case "moreRows":
                    res.sendAsRawJson(new MoreRowsGenerator(injector).generate(req));
                    return;
                default:
                    res.sendUnknownActionError();
            }
        }
        catch (Be5Exception e)
        {
            sendError(req, res, url, e);
        }
        catch (Throwable e)
        {
            sendError(req, res, url, Be5Exception.internalInQuery(e, query));
        }
    }

    private final static String TOP_FORM = "topForm";
    //private final static String TOP_DOCUMENT = "topDocument";

    private void sendQueryResponseData(Request req, Response res, HashUrl url, Object data, Query query)
    {
        ArrayList<ResourceData> included = new ArrayList<>();

        String topForm = (String)ParseRequestUtils.getValuesFromJson(query.getLayout()).get(TOP_FORM);
        if(topForm != null)
        {
            Operation operation = operationExecutor.create(query.getEntity().getName(), query.getName(), topForm);

            Either<FormPresentation, OperationResult> dataTopForm = documentGenerator.generateForm(operation, Collections.emptyMap());
            included.add(new ResourceData(TOP_FORM, dataTopForm.isFirst() ? FORM_ACTION : OPERATION_RESULT,
                    dataTopForm.get(),
                    Collections.singletonMap(SELF_LINK, operation.getUrl().toString())));
        }

        res.sendAsJson(
                new ResourceData(TABLE_ACTION, data, Collections.singletonMap(SELF_LINK, url.toString())),
                included.toArray(new ResourceData[0]),
                req.getDefaultMeta()
        );
    }

    private void sendError(Request req, Response res, HashUrl url, Be5Exception e)
    {
        String message = "";

        //message += GroovyRegister.getErrorCodeLine(e, query.getQuery());

        res.sendErrorAsJson(
                new ErrorModel(e, message, Collections.singletonMap(SELF_LINK, url.toString())),
                req.getDefaultMeta()
        );
    }

}
