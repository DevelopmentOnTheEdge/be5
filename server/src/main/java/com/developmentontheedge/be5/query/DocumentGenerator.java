package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.TablePresentation;
import com.developmentontheedge.be5.model.jsonapi.ErrorModel;
import com.developmentontheedge.be5.model.jsonapi.JsonApiModel;
import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.util.Either;
import com.developmentontheedge.be5.util.HashUrl;

import java.util.Map;


public interface DocumentGenerator
{
    Object routeAndRun(Query query, Map<String, String> parameters);

    Object routeAndRun(Query query, Map<String, String> parameters, int sortColumn, boolean sortDesc);

    StaticPagePresentation getStatic(Query query);

    TablePresentation getTable(Query query, Map<String, String> parameters);

    TablePresentation getTable(Query query, Map<String, String> parameters, int sortColumn, boolean sortDesc);

    TablePresentation getTable(Query query, Map<String, String> parameters, TableModel tableModel);

    Either<FormPresentation, OperationResult> generateForm(Operation operation, Map<String, ?> values);

    Either<FormPresentation, OperationResult> executeForm(Operation operation, Map<String, ?> values);

    ErrorModel getErrorModel(Throwable e, HashUrl url);

    /* JsonApiModel */

    JsonApiModel getDocument(Query query, Map<String, String> parameters);

    JsonApiModel getDocument(Query query, Map<String, String> parameters, int sortColumn, boolean sortDesc);
}
