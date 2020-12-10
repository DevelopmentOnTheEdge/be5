package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.List;
import java.util.Map;


public interface DocumentGenerator
{
    JsonApiModel getDocument(Query query, Map<String, Object> parameters);

    JsonApiModel getDocument(String entityName, String queryName, Map<String, Object> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, List<QRec> rows);

    JsonApiModel getNewTableRows(String entityName, String queryName, Map<String, Object> parameters);

    JsonApiModel getTableRowsAsJson(String entityName, String queryName, Map<String, Object> parameters);

    JsonApiModel getTableTotalNumberOfRows(String entityName, String queryName, Map<String, Object> params);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters);

    void addDocumentPlugin(String name, DocumentPlugin documentPlugin);
}
