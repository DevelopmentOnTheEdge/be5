package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.components.impl.model.ActionHelper;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.components.impl.QueryRouter.Runner;
import com.developmentontheedge.be5.components.impl.model.Operations;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.components.impl.model.TableModel.ColumnModel;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationSet;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.Action;
import com.developmentontheedge.be5.model.TableOperationPresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.components.FrontendConstants.*;
import static com.developmentontheedge.be5.components.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.components.RestApiConstants.TIMESTAMP_PARAM;

public class DocumentGenerator implements Runner {
    /**
     * Generates a response by the given category and the page.
     * Parameters:
     * <ul>
     *   <li>category</li>
     *   <li>page</li>
     *   <li>values?</li>
     * </ul>
     */
    public static void generateAndSend(Request req, Response res, Injector injector) {
        QueryRouter.on(req, injector).run(new DocumentGenerator(req, res, injector));
    }
    
    private final Request req;
    private final Response res;
    private final Injector injector;
    private final UserAwareMeta userAwareMeta;
    
    public DocumentGenerator(Request req, Response res, Injector injector)
    {
        this.req = req;
        this.res = res;
        this.injector = injector;
        this.userAwareMeta = injector.get(UserAwareMeta.class);
    }
    
    @Override
    public void onStatic(Query query)
    {
        String content = query.getProject().getStaticPageContent(UserInfoHolder.getLanguage(), query.getQuery().trim());
        //todo add StaticPagePresentation - title, id, content
        sendQueryResponse(req, res, query, content);
        DocumentResponse.of(res).sendStaticPage(content);
    }

//    private Either<FormPresentation, FrontendAction> getFormPresentation(String entityName, String queryName, String operationName,
//            Operation operation, Map<String, String> presetValues)
//    {
//        return new FormGenerator(injector).generate(entityName, queryName, operationName, operation, presetValues, req);
//    }

    @Override
    public void onTable(Query query, Map<String, String> parametersMap)
    {
        sendQueryResponse(req, res, query, getTablePresentation(query, parametersMap));
        //DocumentResponse.of(res, req).send(getTablePresentation(query, parametersMap));
    }

    @Override
    public void onTable(Query query, Map<String, String> parametersMap, TableModel tableModel)
    {
        sendQueryResponse(req, res, query, getTablePresentation(query, parametersMap, tableModel));
        //DocumentResponse.of(res).send(getTablePresentation(query, parametersMap, tableModel));
    }

    private void sendQueryResponse(Request req, Response res, Query query, Object data)
    {
        res.sendAsJson(
                new ResourceData(TABLE_ACTION, data),
                ImmutableMap.builder()
                        .put(TIMESTAMP_PARAM, req.get(TIMESTAMP_PARAM))
                        .build(),
                Collections.singletonMap(SELF_LINK, ActionHelper.toAction(query).arg)
        );
    }

    private TablePresentation getTablePresentation(Query query, Map<String, String> parametersMap, TableModel table)
    {
        List<TableOperationPresentation> operations = collectOperations(query);

        List<Object> columns = table.getColumns().stream().map(ColumnModel::getTitle).collect(Collectors.toList());
        List<InitialRow> rows = new InitialRowsBuilder(table.isSelectable()).build(table);
        Long totalNumberOfRows = table.getTotalNumberOfRows();

        String entityName = query.getEntity().getName();
        String queryName = query.getName();
        String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(query.getEntity());
        String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(entityName, queryName);
        String title = localizedEntityTitle + ": " + localizedQueryTitle;

        if( totalNumberOfRows == null )
            totalNumberOfRows = TableModel.from(query, parametersMap, req, injector).count();

        return new TablePresentation(title, entityName, queryName, operations, table.isSelectable(), columns, rows, table.getRows().size(),
                parametersMap, totalNumberOfRows, table.isHasAggregate(), getLayoutObject(query));
    }

    private Object getLayoutObject(Query query)
    {
        if (query.getLayout().length() > 0)
        {
            return JsonFactory.jsonb.fromJson(query.getLayout(),
                    new HashMap<String, String>(){}.getClass().getGenericSuperclass());
        }
        else
        {
            return new HashMap<>();
        }
    }

    public TablePresentation getTablePresentation(Query query, Map<String, String> parametersMap)
    {
        List<TableOperationPresentation> operations = collectOperations(query);
        final boolean selectable = !operations.isEmpty() && query.getType() == QueryType.D1;
        int limit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if (limit == 0)
        {
            limit = 20;
        }

        TableModel table = TableModel
                .from(query, parametersMap, req, selectable, injector)
                .limit(limit)
                .build();

        return getTablePresentation(query, parametersMap, table);
    }

    @Override
    public void onParametrizedTable(Query query, Map<String, String> parametersMap)
    {
//        TODO String entityName = query.getEntity().getName();
//        String operationName = query.getParametrizingOperationName();
//        Operation operation = query.getParametrizingOperation();
//        FormPresentation formPresentation = getFormPresentation(entityName, query.getName(), operationName, operation, parametersMap).getFirst();
//        TablePresentation tablePresentation = getTablePresentation(query, parametersMap);
//        FormTable formTable = new FormTable(formPresentation, tablePresentation);
//
//        DocumentResponse.of(res).send(formTable);
        onTable(query, parametersMap);
    }
    
    @Override
    public void onError(String message)
    {
        res.sendError(message);
    }
    
    private List<TableOperationPresentation> collectOperations(Query query) {
        List<TableOperationPresentation> operations = new ArrayList<>();
        List<String> userRoles = UserInfoHolder.getCurrentRoles();

        for (Operation operation : getQueryOperations(query))
        {
            if (Operations.isAllowed(operation, userRoles))
            {
                operations.add(presentOperation(query, operation));
            }
        }

        return operations;
    }

    private List<Operation> getQueryOperations(Query query)
    {
        List<Operation> queryOperations = new ArrayList<>();
        OperationSet operationNames = query.getOperationNames();
        
        for (String operationName : operationNames.getFinalValues())
        {
            Operation op = query.getEntity().getOperations().get(operationName);
            if ( op != null )
                queryOperations.add( op );
        }
        
        return queryOperations;
    }

    private TableOperationPresentation presentOperation(Query query, Operation operation) {
        String visibleWhen = Operations.determineWhenVisible(operation);
        String title = userAwareMeta.getLocalizedOperationTitle(query.getEntity().getName(), operation.getName());
        boolean requiresConfirmation = operation.isConfirm();
        boolean isClientSide = Operations.isClientSide(operation);
        Action action = null;
        
        if (isClientSide)
        {
            action = Action.call(Operations.asClientSide(operation).toHashUrl());
        }
                
        return new TableOperationPresentation(operation.getName(), title, visibleWhen, requiresConfirmation, isClientSide, action);
    }


}
