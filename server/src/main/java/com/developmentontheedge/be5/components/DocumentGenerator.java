package com.developmentontheedge.be5.components;

import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.StaticPagePresentation;
import com.developmentontheedge.be5.model.TablePresentation;

import java.util.Map;


public interface DocumentGenerator
{
    Object routeAndRun(Query query, Map<String, String> parametersMap);

    Object routeAndRun(Query query, Map<String, String> parametersMap, int sortColumn, boolean sortDesc);

    StaticPagePresentation getStatic(Query query);

    TablePresentation getTable(Query query, Map<String, String> parametersMap);

    TablePresentation getTable(Query query, Map<String, String> parametersMap, int sortColumn, boolean sortDesc);

    TablePresentation getTable(Query query, Map<String, String> parametersMap, TableModel tableModel);

    TablePresentation getParametrizedTable(Query query, Map<String, String> parametersMap, int sortColumn, boolean sortDesc);

}
