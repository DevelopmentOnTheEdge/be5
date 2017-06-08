package com.developmentontheedge.be5.legacy;

//import com.developmentontheedge.enterprise.DbmsConnector;
//import com.developmentontheedge.enterprise.UserInfo;
//import com.developmentontheedge.be5.metadata.DatabaseConstants;
//import com.developmentontheedge.be5.HttpOperation;
//import com.developmentontheedge.be5.metadata.Utils;
//import com.developmentontheedge.be5.WebAppInitializer;
//import com.developmentontheedge.be5.api.Request;
//import com.developmentontheedge.be5.api.ServiceProvider;
//import com.developmentontheedge.be5.api.exceptions.Be5Exception;
//import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
//import com.developmentontheedge.be5.api.experimental.Be5Operation;
//import com.developmentontheedge.be5.env.Classes;
//import com.developmentontheedge.be5.metadata.model.Operation;
//import com.developmentontheedge.be5.operations.JavaScriptOperation;
//import com.developmentontheedge.be5.operations.MethodWrapperOperation;
//import com.developmentontheedge.be5.operations.OfflineJavaScriptOperation;
//import com.developmentontheedge.be5.operations.SQLOperation;
//import com.developmentontheedge.be5.operations.SilentSqlOperation;
//import com.google.common.collect.Iterables;

public class LegacyOperationFactory {
    
//    private final ServiceProvider injector;
//    private final DbmsConnector connector;
//    private final UserInfo user;
//	private final HttpServletRequest request;
//
//    public LegacyOperationFactory(ServiceProvider injector, UserInfo user, HttpServletRequest httpServletRequest) {
//        this.injector = injector;
//        this.connector = injector.getDbmsConnector();
//        this.user = user;
//        this.request = httpServletRequest;
//        // Cache color scheme in session
//        Utils.findColorScheme(httpServletRequest, connector, user, null);
//    }
//
//    /**
//     * Returns a legacy operation
//     * @throws Be5Exception if something wrong occurred
//     */
//    public LegacyOperation create(Operation meta, Request req, String fromQuery, Iterable<String> selectedRows) throws Be5Exception {
//        String code = Utils.putPlaceholders(connector, meta.getCode(), user, null);
//        //UserInfo safeUserInfo = new SafeUserInfo(user);
//        class Be5OperationUserInfo extends Utils.OperationUserInfo
//        {
//            Be5OperationUserInfo(UserInfo orig) {
//                this.userName = orig.getUserName();
//                this.curRoleList = orig.getCurRoleList();
//                this.locale = orig.getLocale();
//                this.remoteAddr = orig.getRemoteAddr();
//                this.session = orig.getSession();
//            }
//
//            @Override
//            public Locale getLocale()
//            {
//                return locale;
//            }
//        }
//
//        UserInfo safeUserInfo = new Be5OperationUserInfo(user);
//
//        com.beanexplorer.enterprise.Operation legacyOperation;
//
//        switch (meta.getType())
//        {
//        case DatabaseConstants.OP_TYPE_SQL:
//            if (user.isAdmin())
//            {
//                legacyOperation = new SQLOperation();
//            }
//            else
//            {
//                legacyOperation = new SilentSqlOperation();
//            }
//            ((SQLOperation) legacyOperation).setCode(code);
//            break;
//        case DatabaseConstants.OP_TYPE_JAVASCRIPT_SERVER:
//            if (JavaScriptOperation.canBeOffline(code))
//            {
//                legacyOperation = new OfflineJavaScriptOperation();
//            }
//            else
//            {
//                legacyOperation = new JavaScriptOperation();
//            }
//            ((JavaScriptOperation) legacyOperation).setCode(code);
//            break;
//        case DatabaseConstants.OP_TYPE_JAVA_FUNCTION:
//            legacyOperation = new MethodWrapperOperation();
//            ((MethodWrapperOperation) legacyOperation).setCode(code);
//            break;
//        default:
//            legacyOperation = Classes.tryInstantiate(code, com.beanexplorer.enterprise.Operation.class);
//            break;
//        }
//
//        if (legacyOperation instanceof HttpOperation)
//        {
//            ((HttpOperation) legacyOperation).setReferer(LegacyUrlParser.GO_BACK_URL);
//        }
//
//        String platform = DatabaseConstants.PLATFORM_HTML;
//        String entityName = meta.getEntity().getName();
//        String primaryKey = meta.getEntity().getPrimaryKey();
//        String[] records = Iterables.toArray(selectedRows, String.class);
//        legacyOperation.setName(meta.getName());
//        legacyOperation.setOrigName(meta.getName());
//        try
//        {
//            legacyOperation.setAppInfo(Utils.getAppInfo(WebAppInitializer.getStoredServletContext(),
//                    connector, null, user));
//		}
//        catch (Exception e)
//        {
//            throw Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(e, meta.getEntity().getName(), meta.getName(), e.getMessage());
//		}
//
//        if (legacyOperation instanceof Be5Operation)
//        {
//            ((Be5Operation) legacyOperation).initialize(req, injector);
//        }
//
//        legacyOperation.initialize(platform, safeUserInfo, entityName, primaryKey, records, fromQuery, null, null);
//        legacyOperation.setSessionAdapter(Utils.createSessionAdapter(request.getSession()));
//
//        // TODO add extenders, see
//        // OperationFragmentHelper#loadOperationExtenders
//
//        return new LegacyOperation(legacyOperation, connector);
//    }
    
}
