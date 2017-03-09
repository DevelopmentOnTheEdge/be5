package com.developmentontheedge.be5.components.impl;

public class FormGenerator
{

//    private final ServiceProvider serviceProvider;
//
//    public FormGenerator(ServiceProvider serviceProvider)
//    {
//        this.serviceProvider = serviceProvider;
//    }
//
//    /**
//     * This can generate an usual form or a view parameters (parametrizing
//     * operation's form). Parameters:
//     * <ul>
//     * <li>category</li>
//     * <li>page</li>
//     * <li>action</li>
//     * <li>selectedRows?</li>
//     * <li>values?</li>
//     * </ul>
//     * @param DbmsConnector
//     * @see Query#getParametrizingOperation()
//     */
//    public Either<FormPresentation, FrontendAction> generate(Request req)
//    {
//        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
//        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
//        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
//        String selectedRowsString = nullToEmpty(req.get(RestApiConstants.SELECTED_ROWS));
//        Map<String, String> presetValues = req.getValues(RestApiConstants.VALUES);
//        Operation operation = UserAwareMeta.get(req, serviceProvider).getOperation(entityName, queryName, operationName);
//
//        return generate(req, entityName, queryName, operationName, selectedRowsString, operation,
//                presetValues, serviceProvider.getDbmsConnector());
//    }
//
//    public FormPresentation generateForm(Request req)
//    {
//        Either<FormPresentation, FrontendAction> result = generate(req);
//
//        if (!result.isFirst())
//        {
//            // Bad operation implementation.
//            throw new IllegalStateException();
//        }
//
//        return result.getFirst();
//    }
//
//    /**
//     * Redirects the request to the form generation.
//     * @param presetValues
//     */
//    public Either<FormPresentation, FrontendAction> generate(
//            String entityName, String operationName, Operation operation,
//            Map<String, String> presetValues, Request req)
//    {
//        checkNotNull(entityName);
//        checkNotNull(operationName);
//        checkNotNull(operation);
//        checkNotNull(req);
//
//        return generate(req, entityName, "", operationName, "", operation, presetValues, serviceProvider.getDbmsConnector());
//    }
//
//    public Either<FormPresentation, FrontendAction> generate(
//            String entityName, String queryName, String operationName, Operation operation,
//            Map<String, String> presetValues, Request req)
//    {
//        checkNotNull(entityName);
//        checkNotNull(queryName);
//        checkNotNull(operationName);
//        checkNotNull(operation);
//        checkNotNull(req);
//
//        return generate(req, entityName, queryName, operationName, "", operation, presetValues, serviceProvider.getDbmsConnector());
//    }
//
//    /**
//     * The base method to generate form.
//     * @param presetValues
//     */
//    private Either<FormPresentation, FrontendAction> generate(Request req, String entityName, String queryName,
//            String operationName, String selectedRowsString, Operation operation, Map<String, String> presetValues,
//            DbmsConnector connector)
//    {
//        UserInfoManager userInfoManager = UserInfoManager.get(req, serviceProvider);
//        UserAwareMeta userAwareMeta = UserAwareMeta.get(req, serviceProvider);
//
//        Iterable<String> selectedRows = Splitter.on(',').split(selectedRowsString);
//
//        UserInfo ui = userInfoManager.getUserInfo();
//        LegacyOperationFactory legacyOperationFactory = serviceProvider.get(LegacyOperationsService.class).createFactory(ui, req.getRawRequest());
//        LegacyOperation legacyOperation = legacyOperationFactory.create(operation, req, Utils.readQueryID(connector, entityName, queryName), selectedRows);
//        ComponentModel model = legacyOperation.getParameters(new StringWriter(), presetValues);
//
//        if (model == null) // => need no parameter, run the operation immediately
//        {
//            return Either.second(
//                    new OperationExecutor(serviceProvider).execute(legacyOperation, presetValues, ui, req));
//        }
//
//        // FIXME should use modern localizations from Meta
//        Map<String, String> l10n = legacyOperation.getLocalizedMessages(userInfoManager.getLocale());
//        LegacyUrlsService legacyUrlsService = serviceProvider.get(LegacyUrlsService.class);
//        Optional<String> customAction = legacyOperation.getCustomAction().map(legacyUrlsService::modernize).map(HashUrl::toString);
//
//        return Either.first(generateFormValue(entityName, queryName, operationName, selectedRowsString, presetValues,
//                model, l10n, userAwareMeta, customAction));
//    }
//
//    /**
//     * Transforms a set of properties, i.e. generates a whole form.
//     */
//    private FormPresentation generateFormValue(String entityName, String queryName, String operationName,
//            String selectedRows, Map<String, String> presetValues, ComponentModel model, Map<String, String> l10n,
//            UserAwareMeta userAwareMeta, Optional<String> customAction)
//    {
//        List<Field> fields = generateFormFields(model, l10n);
//        String title = userAwareMeta.getLocalizedOperationTitle(entityName, operationName);
//        return new FormPresentation(entityName, queryName, operationName, title, selectedRows, fields, presetValues, customAction.orElse(null));
//    }
//
//    private List<Field> generateFormFields(ComponentModel model, Map<String, String> l10n)
//    {
//        List<Field> fields = new ArrayList<>();
//        int mode = Property.SHOW_PREFERRED;
//        int nProperties = model.getVisibleCount(mode);
//
//        // TODO add groups
//        for (int iProperty = 0; iProperty < nProperties; iProperty++)
//        {
//            BeProperty property = new BeProperty(model.getVisiblePropertyAt(iProperty, mode));
//
//            if (property.isJavaClassProperty())
//                continue;
//
//            fields.add(generateField(property, l10n));
//        }
//
//        return fields;
//    }
//
//    private Field generateField(BeProperty property, Map<String, String> l10n)
//    {
//        String name = property.getName();
//        String title = property.getTitle(l10n);
//        boolean isReadOnly = property.isReadOnly();
//        boolean canBeNull = property.canBeNull();
//        boolean reloadOnChange = property.reloadOnChange();
//
//        FieldClarifyingFeatures.Builder tipsBuilder = FieldClarifyingFeatures.builder();
//
//        property.getPlaceholder().ifPresent(tipsBuilder::placeholder);
//        property.getHelpText().ifPresent(tipsBuilder::helpText);
//        property.getTooltip().ifPresent(tipsBuilder::tooltip);
//
//        Field.Builder builder = Field.builder(name, title, isReadOnly, canBeNull, reloadOnChange, tipsBuilder.build());
//
//        if (property.isInGroup())
//        {
//            builder.group(property.getGroupId(), property.getGroupName());
//        }
//
//        if (property.autoRefresh())
//        {
//            builder.autoRefresh(true);
//        }
//
//        if (property.isDate())
//        {
//            return builder.date(property.getAsStr());
//        }
//
//        if (property.isDateTime())
//        {
//            return builder.dateTime(property.getAsStr());
//        }
//
//        if (property.isBool())
//        {
//            return builder.checkBox(property.getAsStr());
//        }
//
//        if (property.isAutoComplete())
//        {
//            Entity entity = serviceProvider.getProject().getEntity(property.getExternalEntityName());
//            String pk = entity.getPrimaryKey();
//            String columnFrom = entity.getName()+"::search::"+pk;
//            Query selectionView = StreamEx.of(entity.getAllReferences()).findFirst(ref -> ref.getColumnsFrom().equalsIgnoreCase(columnFrom))
//                    .map(TableReference::getViewName).map(entity.getQueries()::get).orElse(null);
//            if(selectionView != null)
//                return builder.autoComplete(property.getAsStr(), selectionView);
//        }
//
//        if (property.isEnum())
//        {
//            return builder.comboBox(property.getAsStr(), property.getEnumOptions());
//        }
//
//        if (property.isMultilineText())
//        {
//            return generateTextArea(property, builder);
//        }
//
//        if (property.isPassword())
//        {
//            return builder.passwordInput(property.getAsStr());
//        }
//
//        return builder.textInput(property.getAsStr());
//    }
//
//    private Field generateTextArea(BeProperty p, Field.Builder builder)
//    {
//        return builder.textArea(p.getAsStr(), p.getNumberOfCulumns().orElse(40), p.getNumberOfRows().orElse(4));
//    }
    
}
