package com.developmentontheedge.be5.api.operationstest.v1;


import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.operationstest.Be5Operation;
import com.developmentontheedge.be5.api.operationstest.HttpOperation;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;

import com.developmentontheedge.be5.metadata.model.Operation;
import com.google.common.collect.Iterables;

import javax.servlet.http.HttpServletRequest;

public class LegacyOperationFactory {
    
    private final ServiceProvider serviceProvider = ServerModules.getServiceProvider();
    private final DatabaseService connector;
	private final HttpServletRequest request;

    public LegacyOperationFactory(HttpServletRequest httpServletRequest) {
        this.connector = serviceProvider.getDatabaseService();
        this.request = httpServletRequest;
        // Cache color scheme in session
        //Utils.findColorScheme(httpServletRequest, connector, user, null);
    }

    /**
     * Returns a legacy operation
     * @throws Be5Exception if something wrong occurred
     */
    public LegacyOperation create(Operation meta, Request req, String fromQuery, Iterable<String> selectedRows) throws Be5Exception {
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

       // UserInfo safeUserInfo = new Be5OperationUserInfo(user);
        Be5Operation operation;



        switch (meta.getType())
        {
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
        default:
            try {
                Class<?> aClass = Class.forName(meta.getCode());
                operation = (Be5Operation)aClass.newInstance();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw Be5Exception.internalInOperation(e, meta);
            }
            break;
        }

        if (operation instanceof HttpOperation)
        {
            ((HttpOperation) operation).setReferer(LegacyUrlParser.GO_BACK_URL);
        }

        String platform = DatabaseConstants.PLATFORM_HTML;
        String entityName = meta.getEntity().getName();
        String primaryKey = meta.getEntity().getPrimaryKey();
        String[] records = Iterables.toArray(selectedRows, String.class);
        operation.setName(meta.getName());
        operation.setOrigName(meta.getName());
        try
        {
//            operation.setAppInfo(Utils.getAppInfo(WebAppInitializer.getStoredServletContext(),
//                    connector, null, user));
		}
        catch (Exception e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(e, meta.getEntity().getName(), meta.getName(), e.getMessage());
		}

//        if (operation instanceof Be5Operation)
//        {
//            ((Be5Operation) operation).initialize(req, serviceProvider);
//        }

        operation.initialize(platform, UserInfoHolder.getUserInfo(), entityName, primaryKey, records, fromQuery, null, null);
        //operation.setSessionAdapter(Utils.createSessionAdapter(request.getSession()));

        // TODO add extenders, see
        // OperationFragmentHelper#loadOperationExtenders

        return new LegacyOperation(operation, connector);
    }
    
}
