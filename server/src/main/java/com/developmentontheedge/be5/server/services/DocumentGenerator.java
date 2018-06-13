package com.developmentontheedge.be5.server.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.model.TablePresentation;
import com.developmentontheedge.be5.server.model.jsonapi.JsonApiModel;

import java.util.Map;


public interface DocumentGenerator
{
    JsonApiModel createStaticPage(String title, String content, String url);

    //todo move to TableGenerator, add methods return ResourceData
    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters);

    TablePresentation getTablePresentation(Query query, Map<String, Object> parameters, TableModel tableModel);

    JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters);

    JsonApiModel getJsonApiModel(Query query, Map<String, Object> parameters, TableModel tableModel);

    JsonApiModel queryJsonApiFor(String entityName, String queryName, Map<String, Object> parameters);

    //todo refactor frontend to JsonApiModel
    Object updateQueryJsonApi(String entityName, String queryName, Map<String, Object> parameters);

    //    StaticPagePresentation getStatic(Query query);
}
