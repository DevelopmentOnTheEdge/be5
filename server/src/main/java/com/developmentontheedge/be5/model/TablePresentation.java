package com.developmentontheedge.be5.model;

import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.components.impl.InitialRow;
import com.developmentontheedge.be5.components.impl.model.TableModel.ColumnModel;

public class TablePresentation
{

    public final String title;
    public final String category;
    public final String page;
    public final List<TableOperationPresentation> operations;
    public final boolean selectable;
    public final List<Object> columns;
    public final List<InitialRow> rows;
    public final int length;
    public final Map<String, String> parameters;
    public final Long totalNumberOfRows;
    public final boolean hasAggregate;

    public TablePresentation(
            String title,
            String category,
            String page,
            List<TableOperationPresentation> operations,
            boolean selectable,
            List<Object> columns,
            List<InitialRow> rows,
            int length,
            Map<String, String> parameters,
            Long totalNumberOfRows,
            boolean hasAggregate)
    {
        this.title = title;
        this.category = category;
        this.page = page;
        this.operations = operations;
        this.selectable = selectable;
        this.columns = columns;
        this.rows = rows;
        this.length = length;
        this.parameters = parameters;
        this.totalNumberOfRows = totalNumberOfRows;
        this.hasAggregate = hasAggregate;
    }

    public String getTitle()
    {
        return title;
    }

    public String getCategory()
    {
        return category;
    }

    public String getPage()
    {
        return page;
    }

    public List<TableOperationPresentation> getOperations()
    {
        return operations;
    }

    public boolean isSelectable()
    {
        return selectable;
    }

    public List<Object> getColumns()
    {
        return columns;
    }

    public List<InitialRow> getRows()
    {
        return rows;
    }

    public int getLength()
    {
        return length;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }

    public Long getTotalNumberOfRows()
    {
        return totalNumberOfRows;
    }

    public boolean isHasAggregate()
    {
        return hasAggregate;
    }
}
