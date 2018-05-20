package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.Map;


public interface TableBuilder
{
    TableBuilder initialize(Query query, Map<String, Object> parameters);

    TableModel getTableModel();
}
