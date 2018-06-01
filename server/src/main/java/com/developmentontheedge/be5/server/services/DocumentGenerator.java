package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.base.util.HashUrl;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.model.FormPresentation;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.util.Either;
import com.developmentontheedge.be5.web.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.web.model.jsonapi.JsonApiModel;

import java.util.Map;


public interface DocumentGenerator
{
    JsonApiModel getStaticPage(String title, String content, String url);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, TableModel tableModel);

    JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters);

    JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters, TableModel tableModel);

    /* Form */

    Either<FormPresentation, OperationResult> generateForm(Operation operation, Map<String, ?> values);

    Either<FormPresentation, OperationResult> executeForm(Operation operation, Map<String, ?> values);

    ErrorModel getErrorModel(Throwable e, HashUrl url);

    //    StaticPagePresentation getStatic(Query query);
}
