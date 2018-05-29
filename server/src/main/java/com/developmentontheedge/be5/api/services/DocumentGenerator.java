package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.base.util.HashUrl;

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
