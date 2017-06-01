package com.developmentontheedge.be5.components.impl;

@Deprecated
public class OperationExecutor
{
//    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(OperationExecutor.class.getName());
//
//    private final ServiceProvider servicesProvider;
//    private final LegacyUrlsService legacyQueriesService;
//
//    public OperationExecutor(ServiceProvider servicesProvider)
//    {
//        this.servicesProvider = servicesProvider;
//        this.legacyQueriesService = servicesProvider.get(LegacyUrlsService.class);
//    }
//
//    public FrontendAction execute(Request req)
//    {
//        DatabaseService connector = servicesProvider.getDatabaseService();
//        String entityName = req.getNonEmpty(RestApiConstants.ENTITY);
//        String queryName = req.getNonEmpty(RestApiConstants.QUERY);
//        String operationName = req.getNonEmpty(RestApiConstants.OPERATION);
//        Iterable<String> selectedRows = Splitter.on(',').split(req.getOrEmpty(RestApiConstants.SELECTED_ROWS));
//        Map<String, String> fieldValues = req.getValues(RestApiConstants.VALUES);
//
//        Operation operation = null;//UserAwareMeta.get(servicesProvider).getOperation(entityName, queryName, operationName);
//        LegacyOperationFactory legacyOperationFactory = new LegacyOperationFactory(req.getRawRequest());//servicesProvider.get(LegacyOperationsService.class).createFactory(ui, req.getRawRequest());
//        LegacyOperation legacyOperation = legacyOperationFactory.create(operation, req, "Utils.readQueryID", selectedRows);
//
//        return execute(legacyOperation, fieldValues, req);
//    }
//
//    public FrontendAction execute(LegacyOperation legacyOperation, Map<String, String> fieldValues, Request req)
//    {
//        StringWriter out = new StringWriter();
//        //LocalizingWriter writer = new LocalizingWriter(ui, out, legacyOperation.getLocalizedMessages(ui.getLocale()));
//
//        legacyOperation.execute(out, fieldValues);
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
//                log.severe("Operation " + legacyOperation + " returned unsupported redirect URL: " + legacyRedirectUrl);
//            }
//
//            return FrontendAction.redirect(new HashUrl(FrontendConstants.TABLE_ACTION, req.get(RestApiConstants.ENTITY), req.get(RestApiConstants.QUERY)).named(new OperationRequest(req).getAll()));
//        }
//
//        return FrontendAction.redirect(parser.modernize());
//    }

}