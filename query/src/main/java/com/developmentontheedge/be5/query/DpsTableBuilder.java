package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.TableModel;

import java.util.Map;


public interface DpsTableBuilder
{
    DpsTableBuilder initialize(Query query, Map<String, Object> parameters);

    TableModel getTableModel();
}
