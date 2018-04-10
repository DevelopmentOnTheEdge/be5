package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.Map;


public interface TableBuilder
{
    TableBuilder initialize(Query query, Map<String, String> parameters);

    TableModel getTableModel();
}
