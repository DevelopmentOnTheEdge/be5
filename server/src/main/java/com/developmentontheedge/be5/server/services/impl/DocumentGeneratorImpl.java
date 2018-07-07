package com.developmentontheedge.be5.server.services.impl;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.GroovyRegister;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.base.util.LayoutUtils;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.OperationSet;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.util.Collections3;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.InitialRow;
import com.developmentontheedge.be5.query.model.InitialRowsBuilder;
import com.developmentontheedge.be5.query.model.MoreRows;
import com.developmentontheedge.be5.query.model.MoreRowsBuilder;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.query.services.TableModelService;
import com.developmentontheedge.be5.server.helpers.ErrorModelHelper;
import com.developmentontheedge.be5.server.model.StaticPagePresentation;
import com.developmentontheedge.be5.server.model.TableOperationPresentation;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.server.services.CategoriesService;
import com.developmentontheedge.be5.server.services.DocumentGenerator;
import com.developmentontheedge.be5.server.services.FormGenerator;
import com.developmentontheedge.be5.server.services.model.Category;
import com.developmentontheedge.be5.server.util.ParseRequestUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.developmentontheedge.be5.base.FrontendConstants.CATEGORY_ID_PARAM;
import static com.developmentontheedge.be5.base.FrontendConstants.STATIC_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TABLE_MORE_ACTION;
import static com.developmentontheedge.be5.base.FrontendConstants.TOP_FORM;
import static com.developmentontheedge.be5.server.RestApiConstants.SELF_LINK;


public class DocumentGeneratorImpl implements DocumentGenerator
{
    private static final Logger log = Logger.getLogger(DocumentGeneratorImpl.class.getName());

    private final UserAwareMeta userAwareMeta;
    private final GroovyRegister groovyRegister;
    private final TableModelService tableModelService;
    private final CategoriesService categoriesService;
    private final UserInfoProvider userInfoProvider;
    private final FormGenerator formGenerator;
    private final ErrorModelHelper errorModelHelper;

    @Inject
    public DocumentGeneratorImpl(
            UserAwareMeta userAwareMeta,
            GroovyRegister groovyRegister,
            CategoriesService categoriesService,
            TableModelService tableModelService,
            UserInfoProvider userInfoProvider,
            FormGenerator formGenerator, ErrorModelHelper errorModelHelper)
    {
        this.userAwareMeta = userAwareMeta;
        this.groovyRegister = groovyRegister;
        this.categoriesService = categoriesService;
        this.tableModelService = tableModelService;
        this.userInfoProvider = userInfoProvider;
        this.formGenerator = formGenerator;
        this.errorModelHelper = errorModelHelper;
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
//        return new FormGenerator(injector).generate(entityName, queryName, operationName, operation, presetValues, req);
//    }

    @Override
    public JsonApiModel createStaticPage(String title, String content, String url)
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
        List<InitialRow> rows = new InitialRowsBuilder(tableModel).build();
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
        if (categoryID != null)
        {
            return categoriesService.getCategoryNavigation(entityName, Long.parseLong(categoryID));
        } else
        {
            return categoriesService.getRootCategory(entityName);
        }
    }

    private List<TableOperationPresentation> collectOperations(Query query)
    {
        List<TableOperationPresentation> operations = new ArrayList<>();
        List<String> userRoles = userInfoProvider.get().getCurrentRoles();

        for (Operation operation : getQueryOperations(query))
        {
            if (isAllowed(operation, userRoles))
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
            if (op != null)
                queryOperations.add(op);
        }

        return queryOperations;
    }

    private TableOperationPresentation presentOperation(Query query, Operation operation)
    {
        String visibleWhen = determineWhenVisible(operation);
        String title = userAwareMeta.getLocalizedOperationTitle(query.getEntity().getName(), operation.getName());
        boolean requiresConfirmation = operation.isConfirm();
        boolean isClientSide = Operation.OPERATION_TYPE_JAVASCRIPT.equals(operation.getType());
        String action = null;
        if (isClientSide)
        {
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
        if (topForm != null)
        {
            Optional<TableOperationPresentation> topFormOperationPresentation =
                    data.getOperations().stream().filter(x -> x.getName().equals(topForm)).findAny();

            if (topFormOperationPresentation.isPresent())
            {
                ResourceData operationResourceData = formGenerator.generate(query.getEntity().getName(), query.getName(), topForm, new String[]{}, parameters, Collections.emptyMap());
                operationResourceData.setId("topForm");

                included.add(operationResourceData);

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
    public JsonApiModel queryJsonApiFor(String entityName, String queryName, Map<String, Object> parameters)
    {
        try
        {
            Query query = userAwareMeta.getQuery(entityName, queryName);
            TableModel tableModel = tableModelService.getTableModel(query, parameters);

            return getJsonApiModel(query, parameters, tableModel);
        } catch (Be5Exception e)
        {
            HashUrl url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters);
            log.log(e.getLogLevel(), "Error in table: " + url.toString(), e);
            return JsonApiModel.error(errorModelHelper.
                    getErrorModel(e, Collections.singletonMap(SELF_LINK, url.toString())), null);
        }
    }

    @Override
    public JsonApiModel updateQueryJsonApi(String entityName, String queryName, Map<String, Object> parameters)
    {
        String url = new HashUrl(TABLE_ACTION, entityName, queryName).named(parameters).toString();
        Map<String, String> links = Collections.singletonMap(SELF_LINK, url);

        try
        {
            Query query = userAwareMeta.getQuery(entityName, queryName);
            TableModel tableModel = tableModelService.getTableModel(query, parameters);

            return JsonApiModel.data(new ResourceData(TABLE_MORE_ACTION, new MoreRows(
                    tableModel.getTotalNumberOfRows().intValue(),
                    tableModel.getTotalNumberOfRows().intValue(),
                    new MoreRowsBuilder(tableModel).build()
            ), links), null);
        } catch (Be5Exception e)
        {
            log.log(e.getLogLevel(), "Error in table: " + url, e);
            return JsonApiModel.error(errorModelHelper.
                    getErrorModel(e, links), null);
        }
    }

    private static String determineWhenVisible(Operation operation)
    {
        switch (operation.getRecords())
        {
            case Operation.VISIBLE_ALWAYS:
            case Operation.VISIBLE_ALL_OR_SELECTED:
                return "always";
            case Operation.VISIBLE_WHEN_ONE_SELECTED_RECORD:
                return "oneSelected";
            case Operation.VISIBLE_WHEN_ANY_SELECTED_RECORDS:
                return "anySelected";
            case Operation.VISIBLE_WHEN_HAS_RECORDS:
                return "hasRecords";
            default:
                throw new AssertionError();
        }
    }

    private static boolean isAllowed(Operation operation, List<String> userRoles)
    {
        return Collections3.containsAny(userRoles, operation.getRoles().getFinalRoles());
    }

}
