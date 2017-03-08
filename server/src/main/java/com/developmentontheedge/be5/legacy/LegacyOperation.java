package com.developmentontheedge.be5.legacy;

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
//import com.developmentontheedge.enterprise.DatabaseConnector;
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
//        @Override
//        public void initContext(Context context, DatabaseConnector connector) {
//            context.setApplicationClassLoader(new Be5ClassLoader());
//        }
//
//    }
    
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
//
//    private final Operation operation;
//    private final DatabaseConnector connector;
//
//    LegacyOperation(Operation operation, DatabaseConnector connector) {
//        this.operation = operation;
//        this.connector = connector;
//
//        if (operation instanceof HttpOperation)
//        {
//            ((HttpOperation) operation).setQueryURL("q");
//        }
//
//        if (operation instanceof JavaScriptOperation)
//        {
//            // allows to scripts to load classes from the application and modules
//            //TODO JavaScriptOperation.setDebuggerSupport(Be5DebuggerSupport.getInstance());
//        }
//    }
//
//    /**
//     * Tries to get a form fields description.
//     * @param presetValues
//     * @return component model or null if operation parameters is null
//     */
//    public ComponentModel getParameters(Writer out, Map<String, String> presetValues) throws Be5Exception {
//        try {
//			Object formParameters = operation.getParameters(out, connector, presetValues);
//
//			if (formParameters == null)
//			{
//			    return null;
//			}
//
//			// TODO invoke extenders
//			// see call of getParameters() in FormEmitter
//			ComponentModel model = ComponentFactory.getModel(formParameters, true);
//
//			return model;
//		} catch (Exception e) {
//			throw rethrow(e, out.toString());
//		}
//    }
//
//    public Operation getRawOperation()
//    {
//    	return operation;
//    }
//
//    /**
//     * Tries to execute the operation.
//     */
//    public void execute(Writer out, Map<String, String> fieldValues, UserInfo ui) throws Be5Exception {
//        try {
//			HashMap<String, String> fieldValuesCopy = new HashMap<>(fieldValues);
//			Object formParameters = operation.getParameters(out, connector, fieldValuesCopy);
//
//			if (formParameters == null)
//			{
//			    formParameters = new DynamicPropertySetSupport(); // avoid NPE in form.readSubmittedValues(...)
//			}
//
//			/*
//			 * When a field is a nullable enum and an user enters an empty value,
//			 * this should be treated as a null.
//			 *
//			 * See WebFormPropertyInspector#readSubmittedValues(Map, Locale)
//			 * that would throw exceptions otherwise.
//			 */
//			for (Entry<String, String> entry : fieldValues.entrySet())
//			{
//			    entry.setValue(Strings.emptyToNull(entry.getValue()));
//			}
//
//			ClientSideValidationFormPropertyInspector form = new ClientSideValidationFormPropertyInspector();
//			form.explore(formParameters);
//			form.readSubmittedValues(fieldValues, ui);
//
//			invokeOperationWithExtenders(operation, out);
//		} catch (Exception e ) {
//			throw rethrow(e, out.toString());
//		}
//    }
//
//    private Be5Exception rethrow(Exception e, String msg) throws Be5Exception {
//        return Be5ErrorCode.INTERNAL_ERROR_IN_OPERATION.rethrow(e, operation.getEntity(), operation.getName(), msg + "\n" + e.getMessage());
//    }
//
//    public boolean isOperationWithFrontendAction()
//    {
//        return operation instanceof Be5OperationWithFrontendAction;
//    }
//
//    public FrontendAction getFrontendAction()
//    {
//        return ((Be5OperationWithFrontendAction) operation).getFrontendAction();
//    }
//
//    /**
//     * Returns a frontend link. This action is called with additional parameters from the filled fields
//     * when the form is submitted.
//     */
//    public Optional<String> getCustomAction()
//    {
//        return Optional.ofNullable(operation instanceof HttpOperation ? ((HttpOperation) operation).getCustomAction() : null);
//    }
//
//    /**
//     * Returns a redirect URL or empty string.
//     */
//    public String getLegacyRedirectUrl() {
//        if (operation instanceof HttpOperation)
//        {
//            return Strings.nullToEmpty(((HttpOperation) operation).getRedirectURL());
//        }
//
//        return "";
//    }
//
//    public Map<String, String> getLocalizedMessages(Locale locale) {
//        Map<String, String> localizedMessages = new HashMap<>();
//
//        try
//        {
//            // DON'T TRY TO ANALYZE IT,
//            // IT HAS BEEN RIPPED FROM THE FormEmitter.generate(),
//            String operationName = operation.getName();
//            Map<?, ?> temp1 = Utils.readLocalizedMessages(connector, locale, "operation.jsp", DatabaseConstants.L10N_TOPIC_PAGE);
//            Map<?, ?> temp2 = SimpleCompositeMaps.extractMap(OperationFragmentHelper.readOperationMessages(connector, new LocaleUserInfo(locale), operation, operation.getEntity(), operationName, temp1));
//            Map<?, ?> operationNameLocalizations = Utils.readLocalizedMessages(connector, locale, operation.getEntity(), DatabaseConstants.L10N_TOPIC_OPERATION_NAME);
//            Map<?, ?> customMessages = Optional.ofNullable(operation.getCustomLocalization(connector)).orElse(new HashMap<>());
//
//            boolean isFeedbackOperation = "interfaceComments".equals(operation.getEntity()) && operationName.equals("Insert");
//            String correctedOperationName = isFeedbackOperation ? "Leave Feedback" : operationName;
//            String localizedOpName = Utils.getMessage(operationNameLocalizations, correctedOperationName);
//
//            assert areStrings(temp2.keySet());
//            assert areStrings(temp2.values());
//            assert areStrings(customMessages.keySet());
//            assert areStrings(customMessages.values());
//
//            @SuppressWarnings("unchecked")
//            Map<String, String> castedTemp2 = (Map<String, String>) temp2;
//            @SuppressWarnings("unchecked")
//            Map<String, String> castedCustomMessages = (Map<String, String>) customMessages;
//            localizedMessages.putAll(castedTemp2);
//            localizedMessages.putAll(castedCustomMessages);
//
//            if (!isFeedbackOperation)
//            {
//                localizedMessages.put(operationName, localizedOpName);
//            }
//        }
//        catch (RuntimeException e)
//        {
//            throw new RuntimeException(e);
//        }
//        catch (Exception e)
//        {
//            // ignore
//        }
//
//        return Collections.unmodifiableMap(localizedMessages);
//    }
//
//    private static boolean areStrings(Iterable<?> iterable) {
//        return Iterables.all(iterable, Predicates.instanceOf(String.class));
//    }
//
//    /**
//     * THIS CODE IS A COPY! DELETE IT AS SOON AS POSSIBLE.
//     * @see Utils#doInvokeOperation(Operation, DatabaseConnector, Writer)
//     *
//     * {@link Utils#doInvokeOperation(Operation, DatabaseConnector, Writer)}
//     */
//    private void invokeOperationWithExtenders(Operation operation, Writer out) throws Exception {
//        OperationFragmentHelper.invokeExtenders("preInvoke", out, connector, operation, null, null);
//        if (!Boolean.TRUE.equals(OperationFragmentHelper.invokeExtenders("skipInvoke", out, connector, operation, null, null)))
//        {
//            operation.invoke(out, connector);
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
//    }
//
//    @Override
//    public String toString()
//    {
//        return operation.getEntity()+"."+operation.getName();
//    }
}
