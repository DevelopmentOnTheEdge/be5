package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.ResponseHelper;
import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.services.CategoriesService;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.api.services.OperationExecutor;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.api.services.DocumentGenerator;
import com.developmentontheedge.be5.api.services.TableModelService;
import com.developmentontheedge.be5.api.services.model.Category;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.servlet.UserInfoHolder;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.query.model.InitialRow;
import com.developmentontheedge.be5.query.model.InitialRowsBuilder;
import com.developmentontheedge.be5.api.services.model.Operations;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationSet;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.TableOperationPresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.util.HashUrlUtils;
import com.developmentontheedge.be5.util.LayoutUtils;
import com.developmentontheedge.be5.util.ParseRequestUtils;
import com.developmentontheedge.beans.json.JsonFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.base.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.FORM_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.OPERATION_RESULT;
import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TOP_FORM;
import static com.developmentontheedge.be5.api.RestApiConstants.SELF_LINK;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private final UserAwareMeta userAwareMeta;
    private final GroovyRegister groovyRegister;
    private final OperationService operationService;
    private final OperationExecutor operationExecutor;
    private final TableModelService tableModelService;
    private final CategoriesService categoriesService;
    private final ResponseHelper responseHelper;

    @Inject
    public DocumentGeneratorImpl(
            UserAwareMeta userAwareMeta,
            GroovyRegister groovyRegister,
            OperationService operationService,
            OperationExecutor operationExecutor,
            CategoriesService categoriesService,
            TableModelService tableModelService,
            ResponseHelper responseHelper)
    {
        this.userAwareMeta = userAwareMeta;
        this.groovyRegister = groovyRegister;
        this.operationService = operationService;
        this.operationExecutor = operationExecutor;
        this.categoriesService = categoriesService;
        this.tableModelService = tableModelService;
        this.responseHelper = responseHelper;
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

    @Override
    public JsonApiModel getStaticPage(String title, String content, String url)
    {
        return JsonApiModel.data(
                new ResourceData(
                        STATIC_ACTION,
                        new StaticPagePresentation(title, content),
                        Collections.singletonMap(SELF_LINK, url)
                ),
                null
        );
    }

    public TablePresentation getTablePresentation(Query query, Map<String, Object> parameters)
    {
        return getTablePresentation(query, parameters, tableModelService.getTableModel(query, parameters));
    }

    public TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, TableModel tableModel)
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

        List<Category> categoryNavigation = getCategoryNavigation(entityName, (String) parameters.get(CATEGORY_ID_PARAM));

        return new TablePresentation(title, entityName, queryName, operations, tableModel.isSelectable(), columns, rows,
                tableModel.orderColumn, tableModel.orderDir, tableModel.offset, tableModel.getRows().size(),
                parameters, totalNumberOfRows, tableModel.isHasAggregate(), LayoutUtils.getLayoutObject(query), categoryNavigation);
    }

    private List<Category> getCategoryNavigation(String entityName, String categoryID)
    {
        if(categoryID != null)
        {
            return categoriesService.getCategoryNavigation(entityName, Long.parseLong(categoryID));
        }
        else
        {
            return categoriesService.getRootCategory(entityName);
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
        boolean isClientSide = Operation.OPERATION_TYPE_JAVASCRIPT.equals(operation.getType());
        String action = null;
        if(isClientSide){
            action = operation.getCode();
        }

        return new TableOperationPresentation(operation.getName(), title, visibleWhen, requiresConfirmation, isClientSide, action);
    }

    @Override
    public JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters)
    {
        return getJsonApiModel(query, parameters, tableModelService.getTableModel(query, parameters));
    }

    @Override
    public JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters, TableModel tableModel)
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
                        operationExecutor.create(userAwareMeta.getOperation(query.getEntity().getName(), query.getName(), topForm), query.getName(), new String[]{}, parameters);

                Either<FormPresentation, OperationResult> dataTopForm = generateForm(operation, Collections.emptyMap());
                included.add(new ResourceData(TOP_FORM, dataTopForm.isFirst() ? FORM_ACTION : OPERATION_RESULT,
                        dataTopForm.get(),
                        Collections.singletonMap(SELF_LINK, HashUrlUtils.getUrl(operation).toString())));


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
                if (UserInfoHolder.isSystemDeveloper())
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
            if (result.getDetails() != null &&
                    result.getDetails().getClass() == Be5Exception.class &&
                    ((Be5Exception) (result.getDetails())).getCause() != null)
            {
                Throwable cause = ((Be5Exception) (result.getDetails())).getCause();

                String message;
                if (cause.getMessage() != null)
                {
                    message = cause.getMessage().split(System.getProperty("line.separator"))[0];
                } else
                {
                    message = cause.getClass().getSimpleName();
                }
                return OperationResult.error(message, null);
            }
            else
            {
                return OperationResult.error(result.getMessage().split(System.getProperty("line.separator"))[0], null);
            }
        }
        else
        {
            return result;
        }
    }

    @Override
    public ErrorModel getErrorModel(Throwable e, HashUrl url)
    {
        String additionalMessage = Be5Exception.getMessage(e);

        //TODO if(UserInfoHolder.isSystemDeveloper())message += groovyRegister.getErrorCodeLine(e);

        return responseHelper.getErrorModel(Be5Exception.internal(e), additionalMessage,
                Collections.singletonMap(SELF_LINK, url.toString()));
    }

}
