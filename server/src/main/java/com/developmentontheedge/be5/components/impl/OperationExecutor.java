package com.developmentontheedge.be5.components.impl;

public class OperationExecutor
{
    
//    private final ServiceProvider servicesProvider;
//    private final LegacyUrlsService legacyQueriesService;
//    private final Logger logger;
//
//    public OperationExecutor(ServiceProvider servicesProvider)
//    {
//        this.servicesProvider = servicesProvider;
//        this.legacyQueriesService = servicesProvider.get(LegacyUrlsService.class);
//        this.logger = servicesProvider.getLogger();
//    }
//
//    public FrontendAction execute(Request req)
//    {
//        DatabaseConnector connector = servicesProvider.getDbmsConnector();
//        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
//        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
//        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
//        Iterable<String> selectedRows = Splitter.on(',').split(req.getOrEmpty(RestApiConstants.SELECTED_ROWS));
//        Map<String, String> fieldValues = req.getValues(RestApiConstants.VALUES);
//
//        Operation operation = UserAwareMeta.get(req, servicesProvider).getOperation(entityName, queryName, operationName);
//        UserInfoManager userInfoManager = UserInfoManager.get(req, servicesProvider);
//        UserInfo ui = userInfoManager.getUserInfo();
//        LegacyOperationFactory legacyOperationFactory = servicesProvider.get(LegacyOperationsService.class).createFactory(ui, req.getRawRequest());
//        LegacyOperation legacyOperation = legacyOperationFactory.create(operation, req, Utils.readQueryID(connector, entityName, queryName), selectedRows);
//
//        return execute(legacyOperation, fieldValues, ui, req);
//    }
//
//    public FrontendAction execute(LegacyOperation legacyOperation, Map<String, String> fieldValues, UserInfo ui, Request req)
//    {
//        StringWriter out = new StringWriter();
//        LocalizingWriter writer = new LocalizingWriter(ui, out, legacyOperation.getLocalizedMessages(ui.getLocale()));
//
//        legacyOperation.execute(writer, fieldValues, ui);
//
//        if (legacyOperation.isOperationWithFrontendAction())
//        {
//            return legacyOperation.getFrontendAction();
//        }
//
//        return adaptLegacyResultAsFrontendAction(legacyOperation, out, req);
//    }
//
//    private FrontendAction adaptLegacyResultAsFrontendAction(LegacyOperation legacyOperation, StringWriter out, Request req)
//    {
//        FrontendAction action = formModernRedirectUrl(legacyOperation, req);
//        String printResult = out.toString().trim();
//
//        if (printResult.isEmpty())
//        {
//            return action;
//        }
//
//        return FrontendAction.renderHtml(printResult);
//    }
//
//    private FrontendAction formModernRedirectUrl(LegacyOperation legacyOperation, Request req)
//    {
//        String legacyRedirectUrl = legacyOperation.getLegacyRedirectUrl();
//
//        if(legacyRedirectUrl.equals(LegacyUrlParser.GO_BACK_URL))
//            return FrontendAction.goBack();
//
//        // Right now legacyRedirectUrl can contain a number that is an identifier of a query.
//        // This kind of result is not considered as a legacy redirect URL.
//        LegacyUrlParser parser = legacyQueriesService.createParser(legacyRedirectUrl);
//
//        if (!parser.isLegacy())
//        {
//            if (!legacyRedirectUrl.isEmpty())
//            {
//                logger.error("Operation " + legacyOperation + " returned unsupported redirect URL: " + legacyRedirectUrl);
//            }
//
//            return FrontendAction.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, req.get(RestApiConstants.ENTITY), req.get(RestApiConstants.QUERY)).named(new OperationRequest(req).getAll()));
//        }
//
//        return FrontendAction.redirect(parser.modernize());
//    }
    
}