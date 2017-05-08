package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperation;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperationFactory;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyUrlsService;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.metadata.model.TableReference;
import com.developmentontheedge.be5.model.Field;
import com.developmentontheedge.be5.model.FieldClarifyingFeatures;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.model.ComponentModel;
import com.developmentontheedge.beans.model.Property;
import com.google.common.base.Splitter;
import one.util.streamex.StreamEx;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

public class FormGenerator
{

    private final ServiceProvider serviceProvider = ServerModules.getServiceProvider();

    public FormGenerator(){}

    /**
     * This can generate an usual form or a view parameters (parametrizing
     * operation's form). Parameters:
     * <ul>
     * <li>category</li>
     * <li>page</li>
     * <li>action</li>
     * <li>selectedRows?</li>
     * <li>values?</li>
     * </ul>
     * @see Query#getParametrizingOperation()
     */
    public Either<FormPresentation, FrontendAction> generate(Request req)
    {
        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);
        Operation operation = UserAwareMeta.get(serviceProvider).getOperation(entityName, queryName, operationName);

        return generate(req, entityName, queryName, operationName, selectedRowsString, operation,
                presetValues, serviceProvider.getDatabaseService());
    }

    public FormPresentation generateForm(Request req)
    {
        Either<FormPresentation, FrontendAction> result = generate(req);

        if (!result.isFirst())
        {
            // Bad operation implementation.
            throw new IllegalStateException();
        }

        return result.getFirst();
    }

    /**
     * Redirects the request to the form generation.
     * @param presetValues
     */
    public Either<FormPresentation, FrontendAction> generate(
            String entityName, String operationName, Operation operation,
            Map<String, String> presetValues, Request req)
    {
        checkNotNull(entityName);
        checkNotNull(operationName);
        checkNotNull(operation);
        checkNotNull(req);

        return generate(req, entityName, "", operationName, "", operation, presetValues, serviceProvider.getDatabaseService());
    }

    public Either<FormPresentation, FrontendAction> generate(
            String entityName, String queryName, String operationName, Operation operation,
            Map<String, String> presetValues, Request req)
    {
        checkNotNull(entityName);
        checkNotNull(queryName);
        checkNotNull(operationName);
        checkNotNull(operation);
        checkNotNull(req);

        return generate(req, entityName, queryName, operationName, "", operation, presetValues, serviceProvider.getDatabaseService());
    }

    /**
     * The base method to generate form.
     * @param presetValues
     */
    private Either<FormPresentation, FrontendAction> generate(Request req, String entityName, String queryName,
                                                              String operationName, String selectedRowsString, Operation operation, Map<String, String> presetValues,
                                                              DatabaseService connector)
    {
        UserAwareMeta userAwareMeta = UserAwareMeta.get(serviceProvider);

        Iterable<String> selectedRows = Splitter.on(',').split(selectedRowsString);

        UserInfo ui = UserInfoHolder.getUserInfo();
        LegacyOperationFactory legacyOperationFactory = new LegacyOperationFactory(req.getRawRequest());//serviceProvider.get(LegacyOperationsService.class).createFactory(ui, req.getRawRequest());
        LegacyOperation legacyOperation = legacyOperationFactory.create(operation, req, "Utils.readQueryID", selectedRows);
        ComponentModel model = legacyOperation.getParameters(new StringWriter(), presetValues);

        if (model == null) // => need no parameter, run the operation immediately
        {
            return Either.second(
                    new OperationExecutor(serviceProvider).execute(legacyOperation, presetValues, ui, req));
        }

        // FIXME should use modern localizations from Meta
        //Map<String, String> l10n = legacyOperation.getLocalizedMessages(UserInfoHolder.getLocale());
        LegacyUrlsService legacyUrlsService = serviceProvider.get(LegacyUrlsService.class);
        Optional<String> customAction = legacyOperation.getCustomAction().map(legacyUrlsService::modernize).map(HashUrl::toString);

        return Either.first(generateFormValue(entityName, queryName, operationName, selectedRowsString, presetValues,
                model, userAwareMeta, customAction));
    }

    /**
     * Transforms a set of properties, i.e. generates a whole form.
     */
    private FormPresentation generateFormValue(String entityName, String queryName, String operationName,
                                               String selectedRows, Map<String, String> presetValues, ComponentModel model,
                                               UserAwareMeta userAwareMeta, Optional<String> customAction)
    {
        List<Field> fields = generateFormFields(operationName, entityName, queryName, model, userAwareMeta);
        String title = userAwareMeta.getLocalizedOperationTitle(entityName, operationName);
        return new FormPresentation(entityName, queryName, operationName, title, selectedRows, fields, presetValues, customAction.orElse(null));
    }

    private List<Field> generateFormFields(String operationName, String entityName, String queryName, ComponentModel model, UserAwareMeta userAwareMeta)
    {
        List<Field> fields = new ArrayList<>();
        int mode = Property.SHOW_PREFERRED;
        int nProperties = model.getVisibleCount(mode);

        // TODO add groups
        for (int iProperty = 0; iProperty < nProperties; iProperty++)
        {
            BeProperty property = new BeProperty(model.getVisiblePropertyAt(iProperty, mode));

//            if (property.isJavaClassProperty())
//                continue;

            fields.add(generateField(operationName, entityName, queryName, property, userAwareMeta));
        }

        return fields;
    }

    private Field generateField(String operationName, String entityName, String queryName, BeProperty property, UserAwareMeta userAwareMeta)
    {
        String name = property.getName();
            String title = userAwareMeta.getFieldTitle(entityName, operationName, queryName, name)
                    .orElse(property.getDisplayName());

        boolean isReadOnly = property.isReadOnly();
        boolean canBeNull = property.canBeNull();
        boolean reloadOnChange = property.reloadOnChange();

        FieldClarifyingFeatures.Builder tipsBuilder = FieldClarifyingFeatures.builder();

        property.getPlaceholder().ifPresent(tipsBuilder::placeholder);
        property.getHelpText().ifPresent(tipsBuilder::helpText);
        property.getTooltip().ifPresent(tipsBuilder::tooltip);

        Field.Builder builder = Field.builder(name, title, isReadOnly, canBeNull, reloadOnChange, tipsBuilder.build());

        if (property.isInGroup())
        {
            builder.group(property.getGroupId(), property.getGroupName());
        }

        if (property.autoRefresh())
        {
            builder.autoRefresh(true);
        }

        if (property.isDate())
        {
            return builder.date(property.getAsStr());
        }

        if (property.isDateTime())
        {
            return builder.dateTime(property.getAsStr());
        }

        if (property.isBool())
        {
            return builder.checkBox(property.getAsStr());
        }

        if (property.isAutoComplete())
        {
            Entity entity = serviceProvider.getProject().getEntity(property.getExternalEntityName());
            String pk = entity.getPrimaryKey();
            String columnFrom = entity.getName()+"::search::"+pk;
            Query selectionView = StreamEx.of(entity.getAllReferences()).findFirst(ref -> ref.getColumnsFrom().equalsIgnoreCase(columnFrom))
                    .map(TableReference::getViewName).map(entity.getQueries()::get).orElse(null);
            if(selectionView != null)
                return builder.autoComplete(property.getAsStr(), selectionView);
        }

        if (property.isEnum())
        {
            return builder.comboBox(property.getAsStr(), property.getEnumOptions());
        }

        if (property.isMultilineText())
        {
            return generateTextArea(property, builder);
        }

        if (property.isPassword())
        {
            return builder.passwordInput(property.getAsStr());
        }

        return builder.textInput(property.getAsStr());
    }

    private Field generateTextArea(BeProperty p, Field.Builder builder)
    {
        return builder.textArea(p.getAsStr(), p.getNumberOfCulumns().orElse(40), p.getNumberOfRows().orElse(4));
    }

}
