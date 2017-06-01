package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public static void generateAndSend(Request req, Response res, ServiceProvider serviceProvider) {
        QueryRouter.on(req, serviceProvider).run(new DocumentGenerator(req, res, serviceProvider));
    }
    
    private final Request req;
    private final Response res;
    private final ServiceProvider serviceProvider;
    private final UserAwareMeta userAwareMeta;
    
    private DocumentGenerator(Request req, Response res, ServiceProvider serviceProvider) {
        this.req = req;
        this.res = res;
        this.serviceProvider = serviceProvider;
        this.userAwareMeta = UserAwareMeta.get(serviceProvider);
    }
    
    @Override
    public void onStatic(Query query)
    {
        DocumentResponse.of(res).sendStaticPage(query.getProject().getStaticPageContent(UserInfoHolder.getLanguage(), query.getQuery().trim()));
    }
    
    @Override
    public void onForm(String entityName, Optional<String> queryName, String operationName, Operation operation, Map<String, String> presetValues)
    {
        //TODO DocumentResponse.of(res).send(getFormPresentation(entityName, queryName.orElse(""), operationName, operation, presetValues));
    }
    
//    private Either<FormPresentation, FrontendAction> getFormPresentation(String entityName, String queryName, String operationName,
//            Operation operation, Map<String, String> presetValues)
//    {
//        return new FormGenerator(serviceProvider).generate(entityName, queryName, operationName, operation, presetValues, req);
//    }

    @Override
    public void onTable(Query query, Map<String, String> parametersMap)
    {
        DocumentResponse.of(res).send(getTablePresentation(query, parametersMap));
    }

    private TablePresentation getTablePresentation(Query query, Map<String, String> parametersMap)
    {
        List<TableOperationPresentation> operations = collectOperations(query);
        //&& !Strings2.isNullOrEmpty( query.getEntity().getPrimaryKey() )
        final boolean selectable = !operations.isEmpty() && query.getType() == QueryType.D1;
        int limit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();
        
        if (limit == 0)
        {
            limit = 20;
        }
        
        TableModel table = TableModel
                .from(serviceProvider, query, parametersMap, req, selectable)
                .limit(limit)
                .build();
        List<Object> columns = table.getColumns().stream().map(ColumnModel::getTitle).collect(Collectors.toList());
        List<InitialRow> rows = new InitialRowsBuilder(selectable).build(table);
        Long totalNumberOfRows = table.getTotalNumberOfRows();
        
        String category = query.getEntity().getName();
        String page = query.getName();
        String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(query.getEntity());
        String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(category, page);
        String title = localizedEntityTitle + ": " + localizedQueryTitle;

        if( totalNumberOfRows == null )
            totalNumberOfRows = TableModel.from(serviceProvider, query, parametersMap, req).count();

        return new TablePresentation(title, category, page, operations, selectable, columns, rows, limit,
                parametersMap, totalNumberOfRows, table.isHasAggregate());
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
    }
    
    @Override
    public void onError(String message)
    {
        res.sendError(message);
    }
    
    private List<TableOperationPresentation> collectOperations(Query query) {
        List<TableOperationPresentation> operations = new ArrayList<>();
        List<String> userRoles = UserInfoHolder.getCurrentRoles();
        
        try
        {
            for (Operation operation : getQueryOperations(query))
            {
                if (Operations.isAllowed(operation, userRoles))
                {
                    operations.add(presentOperation(query, operation));
                }
            }
        }
        catch (RuntimeException e)
        {
            throw new AssertionError("", e);
        }
        catch (Exception e1)
        {
            // can't read roles
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
