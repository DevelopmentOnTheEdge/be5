package com.developmentontheedge.be5.components.impl;

import com.developmentontheedge.be5.api.FrontendAction;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperation;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyOperationFactory;
import com.developmentontheedge.be5.api.operationstest.v1.LegacyUrlsService;
import com.developmentontheedge.be5.components.RestApiConstants;
import com.developmentontheedge.be5.env.ServerModules;
import com.developmentontheedge.be5.metadata.model.Operation;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.base.Splitter;

import java.io.StringWriter;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.nullToEmpty;

@Deprecated
public class FormGenerator
{

    private final ServiceProvider serviceProvider = ServerModules.getServiceProvider();

    public FormGenerator(){}

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
//        return generate(req, entityName, "", operationName, "", operation, presetValues);
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
//        return generate(req, entityName, queryName, operationName, "", operation, presetValues);
//    }

}
