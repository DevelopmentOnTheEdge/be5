package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.impl.TableModel;

import java.util.Map;


public interface TableModelService
{
    TableModel getTableModel(Query query, Map<String, ?> parameters);

    TableModel.Builder builder(Query query, Map<String, ?> parameters);
}
