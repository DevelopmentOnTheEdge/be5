package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.api.services.model.Category;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.query.model.InitialRow;
import com.developmentontheedge.be5.query.model.InitialRowsBuilder;
import com.developmentontheedge.be5.query.impl.Operations;
import com.developmentontheedge.be5.query.impl.TableModel;
import com.developmentontheedge.be5.query.impl.TableModel.ColumnModel;
import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationSet;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.Action;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.TableOperationPresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.query.TableBuilder;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.api.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.api.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.api.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.api.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.api.FrontendConstants.TOP_FORM;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;
import static com.developmentontheedge.be5.api.RestApiConstants.LIMIT;
import static com.developmentontheedge.be5.api.RestApiConstants.OFFSET;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_COLUMN;
import static com.developmentontheedge.be5.api.RestApiConstants.ORDER_DIR;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private final UserAwareMeta userAwareMeta;
    private final CoreUtils coreUtils;
    private final GroovyRegister groovyRegister;
    private final Injector injector;
    private final OperationService operationService;
    private final OperationExecutor operationExecutor;
    private final Provider<CategoriesService> categoriesService;

    public DocumentGeneratorImpl(
            CoreUtils coreUtils,
            UserAwareMeta userAwareMeta,
            GroovyRegister groovyRegister,
            OperationService operationService,
            OperationExecutor operationExecutor,
            Provider<CategoriesService> categoriesService,
            Injector injector)
    {
        this.coreUtils = coreUtils;
        this.userAwareMeta = userAwareMeta;
        this.groovyRegister = groovyRegister;
        this.operationService = operationService;
        this.operationExecutor = operationExecutor;
        this.categoriesService = categoriesService;
        this.injector = injector;
    }

    @Override
    public TableModel getTableModel(Query query, Map<String, String> parameters)
    {
        switch (query.getType())
        {
            case D1:
            case D1_UNKNOWN:
                return getSqlTableModel(query, parameters);
            case GROOVY:
                return getGroovyTableModel(query, parameters);
            default:
                throw Be5Exception.internal("Unknown action type '" + query.getType() + "'");
        }
    }

    private TableModel getSqlTableModel(Query query, Map<String, String> parameters)
    {
        int orderColumn = Integer.parseInt(parameters.getOrDefault(ORDER_COLUMN, "-1"));
        String orderDir = parameters.getOrDefault(ORDER_DIR, "asc");
        int offset      = Integer.parseInt(parameters.getOrDefault(OFFSET, "0"));
        int limit = Integer.parseInt(parameters.getOrDefault(LIMIT, Integer.toString(Integer.MAX_VALUE)));

        parameters.remove(ORDER_COLUMN);
        parameters.remove(ORDER_DIR);
        parameters.remove(OFFSET);
        parameters.remove(LIMIT);

        int maxLimit = userAwareMeta.getQuerySettings(query).getMaxRecordsPerPage();

        if(limit == Integer.MAX_VALUE)
        {
            //todo move defaultPageLimit, to getQuerySettings(query)
            limit = Integer.parseInt(getLayoutObject(query).getOrDefault("defaultPageLimit",
                    coreUtils.getSystemSetting("be5_defaultPageLimit", "10")).toString());
        }

        return TableModel
                .from(query, parameters, injector)
                .sortOrder(orderColumn, orderDir)
                .offset(offset)
                .limit(Math.min(limit, maxLimit))
                .build();
    }

    private TableModel getGroovyTableModel(Query query, Map<String, String> parameters)
    {
        try
        {
            Class aClass = groovyRegister.getClass(query.getEntity() + query.getName(),
                    query.getQuery(), query.getFileName());

            if(aClass != null)
            {
                TableBuilder tableBuilder = (TableBuilder) aClass.newInstance();

                tableBuilder.initialize(query, parameters);
                injector.injectAnnotatedFields(tableBuilder);

                return tableBuilder.getTableModel();
            }
            else
            {
                throw Be5Exception.internal("Class " + query.getQuery() + " is null." );
            }
        }
        catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
        {
            throw new UnsupportedOperationException( "Groovy feature has been excluded", e );
        }
    }
//
//    @Override
//    public StaticPagePresentation getStatic(Query query)
//    {
//        String content = query.getProject().getStaticPageContent(UserInfoHolder.getLanguage(), query.getQuery().trim());
//
//        String entityName = query.getEntity().getName();
//        String queryName = query.getName();
//        String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(entityName, queryName);
//
//        return new StaticPagePresentation(localizedQueryTitle, content);
//    }

//    private Either<FormPresentation, FrontendAction> getFormPresentation(String entityName, String queryName, String operationName,
//            Operation operation, Map<String, String> presetValues)
//    {
//        return new FormGenerator(injector).generateForm(entityName, queryName, operationName, operation, presetValues, req);
//    }

    public TablePresentation getTablePresentation(Query query, Map<String, String> parameters)
    {
        return getTablePresentation(query, parameters, getTableModel(query, parameters));
    }

    public TablePresentation getTablePresentation(Query query, Map<String, String> parameters, TableModel tableModel)
    {
        List<Object> columns = tableModel.getColumns().stream().map(ColumnModel::getTitle).collect(Collectors.toList());
        List<InitialRow> rows = new InitialRowsBuilder(tableModel.isSelectable()).build(tableModel);
        Long totalNumberOfRows = tableModel.getTotalNumberOfRows();

        String entityName = query.getEntity().getName();
        String queryName = query.getName();
        String localizedEntityTitle = userAwareMeta.getLocalizedEntityTitle(query.getEntity());
        String localizedQueryTitle = userAwareMeta.getLocalizedQueryTitle(entityName, queryName);
        String title = localizedEntityTitle + ": " + localizedQueryTitle;

        List<TableOperationPresentation> operations = collectOperations(query);

        List<Category> categoryNavigation = getCategoryNavigation(entityName, parameters.get(CATEGORY_ID_PARAM));

        return new TablePresentation(title, entityName, queryName, operations, tableModel.isSelectable(), columns, rows,
                tableModel.orderColumn, tableModel.orderDir, tableModel.offset, tableModel.getRows().size(),
                parameters, totalNumberOfRows, tableModel.isHasAggregate(), getLayoutObject(query), categoryNavigation);
    }

    private List<Category> getCategoryNavigation(String entityName, String categoryID)
    {
        if(categoryID != null)
        {
            return categoriesService.get().getCategoryNavigation(Long.parseLong(categoryID));
        }
        else
        {
            return categoriesService.get().getRootCategory(entityName);
        }
    }

    private List<TableOperationPresentation> collectOperations(Query query)
    {
        List<TableOperationPresentation> operations = new ArrayList<>();
        List<String> userRoles = UserInfoHolder.getCurrentRoles();

        for (Operation operation : getQueryOperations(query))
        {
            if (Operations.isAllowed(operation, userRoles))
            {
                operations.add(presentOperation(query, operation));
            }
        }

        operations.sort(Comparator.comparing(TableOperationPresentation::getTitle));

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

    private TableOperationPresentation presentOperation(Query query, Operation operation)
    {
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

    @Override
    public JsonApiModel getJsonApiModel(Query query, Map<String, String> parameters)
    {
        return getJsonApiModel(query, parameters, getTableModel(query, parameters));
    }

    @Override
    public JsonApiModel getJsonApiModel(Query query, Map<String, String> parameters, TableModel tableModel)
    {
        TablePresentation data = getTablePresentation(query, parameters, tableModel);
        HashUrl url = new HashUrl(TABLE_ACTION, query.getEntity().getName(), query.getName()).named(parameters);

        List<ResourceData> included = new ArrayList<>();

        String topForm = (String) ParseRequestUtils.getValuesFromJson(query.getLayout()).get(TOP_FORM);
        if(topForm != null)
        {
            Optional<TableOperationPresentation> topFormOperationPresentation =
                    data.getOperations().stream().filter(x -> x.getName().equals(topForm)).findAny();

            if(topFormOperationPresentation.isPresent())
            {
                com.developmentontheedge.be5.operation.Operation operation =
                        operationExecutor.create(query.getEntity().getName(), query.getName(), topForm, new String[]{}, parameters);

                Either<FormPresentation, OperationResult> dataTopForm = generateForm(operation, Collections.emptyMap());
                included.add(new ResourceData(TOP_FORM, dataTopForm.isFirst() ? FORM_ACTION : OPERATION_RESULT,
                        dataTopForm.get(),
                        Collections.singletonMap(SELF_LINK, operation.getUrl().toString())));


                data.getOperations().remove(topFormOperationPresentation.get());
            }
        }

        return JsonApiModel.data(
                new ResourceData(TABLE_ACTION, data, Collections.singletonMap(SELF_LINK, url.toString())),
                included.toArray(new ResourceData[0]),
                null
        );
    }

    @Override
    public Either<FormPresentation, OperationResult> generateForm(com.developmentontheedge.be5.operation.Operation operation,
                                                                  Map<String, ?> values)
    {
        return processForm(operation, values, false);
    }

    @Override
    public Either<FormPresentation, OperationResult> executeForm(com.developmentontheedge.be5.operation.Operation operation,
                                                                 Map<String, ?> values)
    {
        return processForm(operation, values, true);
    }

    private Either<FormPresentation, OperationResult> processForm(com.developmentontheedge.be5.operation.Operation operation,
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
                if(UserInfoHolder.isSystemDeveloper())
                {
                    errorModel = getErrorModel((Throwable) operation.getResult().getDetails(), operation.getUrl());
                }

                if(operation.getResult().getDetails() != null &&
                   operation.getResult().getDetails().getClass() == Be5Exception.class &&
                   ((Be5Exception)(operation.getResult().getDetails())).getCause() != null)
                {
                    Throwable cause = ((Be5Exception) (operation.getResult().getDetails())).getCause();

                    String message;
                    if(cause.getMessage() != null){
                        message = cause.getMessage().split(System.getProperty("line.separator"))[0];
                    }else{
                        message = cause.getClass().getSimpleName();
                    }
                    operation.setResult(OperationResult.error(message, null));
                }
                else
                {
                    operation.setResult(OperationResult.error(operation.getResult().getMessage().split(System.getProperty("line.separator"))[0], null));
                }
            }

            return Either.first(new FormPresentation(
                    operation.getInfo(),
                    operation.getContext(),
                    userAwareMeta.getLocalizedOperationTitle(operation.getInfo()),
                    JsonFactory.bean(result.getFirst()),
                    getLayoutObject(operation.getInfo().getModel()),
                    operation.getResult(),
                    errorModel
            ));
        }
        else
        {
            if(operation.getResult().getStatus() == OperationStatus.ERROR){
                //remove Throwable for prevent adding to json
                return Either.second(OperationResult.error(result.getSecond().getMessage(), null));
            }else{
                return Either.second(result.getSecond());
            }
        }
    }

    @Override
    public ErrorModel getErrorModel(Throwable e, HashUrl url)
    {
        String message = Be5Exception.getMessage(e);

        if(UserInfoHolder.isSystemDeveloper())message += groovyRegister.getErrorCodeLine(e);

        return new ErrorModel("500", e.getMessage(), message, Be5Exception.exceptionAsString(e),
                Collections.singletonMap(SELF_LINK, url.toString()));
    }

    @Override
    public Map<String, Object> getLayoutObject(EntityItem entityItem)
    {
        if (!entityItem.getLayout().isEmpty())
        {
            return JsonFactory.jsonb.fromJson(entityItem.getLayout(),
                    new HashMap<String, Object>(){}.getClass().getGenericSuperclass());
        }
        else
        {
            return new HashMap<>();
        }
    }
}
