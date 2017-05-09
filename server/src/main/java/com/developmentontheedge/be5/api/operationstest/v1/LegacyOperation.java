package com.developmentontheedge.be5.api.operationstest.v1;

//import java.io.Writer;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Locale;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Optional;
//
//import com.beanexplorer.beans.DynamicPropertySetSupport;
//import com.beanexplorer.model.ComponentFactory;
//import com.beanexplorer.model.ComponentModel;
//import com.beanexplorer.util.SimpleCompositeMap;
//import com.developmentontheedge.enterprise.DbmsConnector;
//import com.developmentontheedge.be5.metadata.DatabaseConstants;
//import com.developmentontheedge.be5.HttpOperation;
//import com.developmentontheedge.be5.Operation;
//import com.developmentontheedge.be5.enterprise.UserInfo;
//import com.developmentontheedge.be5.metadata.Utils;
//import com.developmentontheedge.be5.api.FrontendAction;
//import com.developmentontheedge.be5.api.exceptions.Be5Exception;
//import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
//import com.developmentontheedge.be5.api.experimental.Be5OperationWithFrontendAction;
//import com.developmentontheedge.be5.operation.OperationFragmentHelper;
//import com.developmentontheedge.be5.operations.JavaScriptOperation;
//import com.developmentontheedge.be5.operations.JavaScriptOperation.DefaultDebuggerSupport;
//import com.developmentontheedge.be5.util.Reflection;
//import com.developmentontheedge.be5.validation.ClientSideValidationFormPropertyInspector;
//import com.google.common.base.Predicates;
//import com.google.common.base.Strings;
//import com.google.common.collect.Iterables;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.exceptions.Be5ErrorCode;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.operationstest.HttpOperation;
import com.developmentontheedge.be5.api.operationstest.Be5Operation;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.developmentontheedge.beans.model.ComponentFactory;
import com.developmentontheedge.beans.model.ComponentModel;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Wraps an old BeanExplorer3 operation to simplify work with it.
 * 
 * @author asko
 */
public class LegacyOperation {
    
//    private static class Be5DebuggerSupport extends DefaultDebuggerSupport {
//
//        private static final Be5DebuggerSupport INSTANCE = new Be5DebuggerSupport();
//
//        public static Be5DebuggerSupport getInstance() {
//            return INSTANCE;
//        }
//
//        private Be5DebuggerSupport() {
//        }
//
//    }
//
//    private final static class SimpleCompositeMaps
//    {
//        /**
//         * It's just a workaround to extract content (as a correct map) of SimpleCompositeMaps.
//         * The better way is to provide correct map interface with SimpleCompositeMaps
//         * as SimpleCompositeMap extends the AbstractMap and therefore implements the Map interface.
//         */
//        @SuppressWarnings({ "rawtypes", "unchecked" })
//        static Map<?, ?> extractMap(Map<?, ?> originalMap) {
//            if (!(originalMap instanceof SimpleCompositeMap))
//                return originalMap;
//
//            SimpleCompositeMap compositeMap = (SimpleCompositeMap) originalMap;
//            Map result = new HashMap<>();
//
//            try
//            {
//                List<Map> maps = (List<Map>) Reflection.on(compositeMap).get("maps");
//                Map me = (Map) Reflection.on(compositeMap).get("me");
//                result.putAll(me);
//
//                for (Map map : maps)
//                {
//                    result.putAll(extractMap(map));
//                }
//
//                return result;
//            }
//            catch (Exception e)
//            {
//                throw new AssertionError("", e); // shouldn't happen
//            }
//        }
//    }

    private final Be5Operation operation;
    private final DatabaseService connector;

    LegacyOperation(Be5Operation operation, DatabaseService connector) {
        this.operation = operation;
        this.connector = connector;

        if (operation instanceof HttpOperation)
        {
            ((HttpOperation) operation).setQueryURL("q");
        }

//        if (operation instanceof JavaScriptOperation)
//        {
//            // allows to scripts to load classes from the application and modules
//            //TODO JavaScriptOperation.setDebuggerSupport(Be5DebuggerSupport.getInstance());
//        }
    }

    /**
     * Tries to get a form fields description.
     * @param presetValues
     * @return component model or null if operation parameters is null
     */
    public ComponentModel getParameters(Writer out, Map<String, String> presetValues) throws Be5Exception {
        try {
			Object formParameters = operation.getParameters(out, connector, presetValues);

			if (formParameters == null)
			{
			    return null;
			}

			// TODO invoke extenders
			// see call of getParameters() in FormEmitter
			ComponentModel model = ComponentFactory.getModel(formParameters);

			return model;
		} catch (Exception e) {
			throw rethrow(e, out.toString());
		}
    }

    public Be5Operation getRawOperation()
    {
    	return operation;
    }

    /**
     * Tries to execute the operation.
     */
    public void execute(Writer out, Map<String, String> fieldValues, UserInfo ui) throws Be5Exception {
        try {
			HashMap<String, String> fieldValuesCopy = new HashMap<>(fieldValues);
			Object formParameters = operation.getParameters(out, connector, fieldValuesCopy);

			if (formParameters == null)
			{
			    formParameters = new DynamicPropertySetSupport(); // avoid NPE in form.readSubmittedValues(...)
			}

			/*
			 * When a field is a nullable enum and an user enters an empty value,
			 * this should be treated as a null.
			 *
			 * See WebFormPropertyInspector#readSubmittedValues(Map, Locale)
			 * that would throw exceptions otherwise.
			 */
			for (Map.Entry<String, String> entry : fieldValues.entrySet())
			{
			    entry.setValue(Strings.emptyToNull(entry.getValue()));
			}

//			ClientSideValidationFormPropertyInspector form = new ClientSideValidationFormPropertyInspector();
//			form.explore(formParameters);
//			form.readSubmittedValues(fieldValues, ui);

			invokeOperationWithExtenders(operation, out);
		} catch (Exception e ) {
			throw rethrow(e, out.toString());
		}
    }

    private Be5Exception rethrow(Exception e, String msg) throws Be5Exception {
        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(e, operation.getEntity(), operation.getName(), msg + "\n" + e.getMessage());
    }

    public boolean isOperationWithFrontendAction()
    {
        return operation instanceof Be5OperationWithFrontendAction;
    }

    public FrontendAction getFrontendAction()
    {
        return ((Be5OperationWithFrontendAction) operation).getFrontendAction();
    }

    /**
     * Returns a frontend link. This action is called with additional parameters from the filled fields
     * when the form is submitted.
     */
    public Optional<String> getCustomAction()
    {
        return Optional.ofNullable(operation instanceof HttpOperation ? ((HttpOperation) operation).getCustomAction() : null);
    }

    /**
     * Returns a redirect URL or empty string.
     */
    public String getLegacyRedirectUrl() {
        if (operation instanceof HttpOperation)
        {
            return Strings.nullToEmpty(((HttpOperation) operation).getRedirectURL());
        }

        return "";
    }

    private static boolean areStrings(Iterable<?> iterable) {
        return Iterables.all(iterable, Predicates.instanceOf(String.class));
    }

    /**
     * THIS CODE IS A COPY! DELETE IT AS SOON AS POSSIBLE.
     * @see Utils #doInvokeOperation(Operation, DbmsConnector, Writer)
     *
     * {@link Utils #doInvokeOperation(Operation, DbmsConnector, Writer)}
     */
    private void invokeOperationWithExtenders(Be5Operation operation, Writer out) throws Exception {
        //OperationFragmentHelper.invokeExtenders("preInvoke", out, connector, operation, null, null);
        operation.invoke(out, connector);
//        if (!Boolean.TRUE.equals(OperationFragmentHelper.invokeExtenders("skipInvoke", out, connector, operation, null, null)))
//        {
//
//            OperationFragmentHelper.invokeExtenders("postInvoke", out, connector, operation, null, null);
//        }
//        else
//        {
//            Object reason = OperationFragmentHelper.invokeExtenders("getSkipInvokeReason", out, connector, operation, null, null);
//            if (reason != null)
//            {
//                out.write("" + reason + "<br />");
//            }
//            else
//            {
//                out.write("{{{Invocation of operation is cancelled by extender}}}.<br />");
//            }
//        }
    }

    @Override
    public String toString()
    {
        return operation.getEntity()+"."+operation.getName();
    }
}
