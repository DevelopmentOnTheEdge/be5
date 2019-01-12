package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.TableModel;

import javax.inject.Inject;
import java.util.Map;

public class TableModelService
{
    private final TableBuilder.TableBuilderFactory tableBuilderFactory;

    @Inject
    public TableModelService(TableBuilder.TableBuilderFactory tableBuilderFactory)
    {
        this.tableBuilderFactory = tableBuilderFactory;
    }

    public TableModel create(Query query, Map<String, Object> parameters)
    {
        return tableBuilderFactory.create(query, parameters).get();
    }
}
