package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.DocumentPlugin;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.Map;


public interface DocumentGenerator
{
    //todo move from DocumentGenerator
    JsonApiModel getStaticPage(String name);

    JsonApiModel createStaticPage(String title, String content, String url);

    JsonApiModel getDocument(Query query, Map<String, Object> parameters);

    JsonApiModel getDocument(String entityName, String queryName, Map<String, Object> parameters);

    JsonApiModel getNewTableRows(String entityName, String queryName, Map<String, Object> parameters);

    void addDocumentPlugin(String name, DocumentPlugin documentPlugin);

    void clearSavedPosition(Query query, Map<String, Object> parameters);
}
