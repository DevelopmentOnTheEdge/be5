package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.metadata.model.EntityItem;
import com.developmentontheedge.be5.query.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.FormPresentation;
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
    /* Table */

    TableModel getTableModel(Query query, Map<String, String> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, String> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, String> parameters, TableModel tableModel);

    JsonApiModel getJsonApiModel(Query query, Map<String, String> parameters);

    JsonApiModel getJsonApiModel(Query query, Map<String, String> parameters, TableModel tableModel);

    Map<String, Object> getLayoutObject(EntityItem entityItem);

    /* Form */

    Either<FormPresentation, OperationResult> generateForm(Operation operation, Map<String, ?> values);

    Either<FormPresentation, OperationResult> executeForm(Operation operation, Map<String, ?> values);

    ErrorModel getErrorModel(Throwable e, HashUrl url);

    //    StaticPagePresentation getStatic(Query query);
}
