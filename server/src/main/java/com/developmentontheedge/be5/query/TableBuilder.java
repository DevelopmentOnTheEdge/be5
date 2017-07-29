package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.Map;

public interface TableBuilder
{
    TableModel get(Query query, Map<String, String> parametersMap, Request req, Injector injector);
}
