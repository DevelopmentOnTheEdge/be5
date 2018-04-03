package com.developmentontheedge.be5.model;

import java.util.List;
import java.util.Map;

import com.developmentontheedge.be5.query.impl.InitialRow;

public class TablePresentation
{
    private final String title;
    private final String category;
    private final String page;
    private final List<TableOperationPresentation> operations;
    private final boolean selectable;
    private final List<Object> columns;
    private final List<InitialRow> rows;

    private final int offset;
    private final int orderColumn;
    private final String orderDir;
    private final int length;

    private final Map<String, String> parameters;
    private final Long totalNumberOfRows;
    private final boolean hasAggregate;
    private final Object layout;

    public TablePresentation(
            String title,
            String category,
            String page,
            List<TableOperationPresentation> operations,
            boolean selectable,
            List<Object> columns,
            List<InitialRow> rows,
            int orderColumn,
            String orderDir,
            int offset,
            int length,
            Map<String, String> parameters,
            Long totalNumberOfRows,
            boolean hasAggregate,
            Object layout)
    {
        this.title = title;
        this.category = category;
        this.page = page;
        this.operations = operations;
        this.selectable = selectable;
        this.columns = columns;
        this.rows = rows;
        this.orderColumn = orderColumn;
        this.orderDir = orderDir;
        this.offset = offset;
        this.length = length;
        this.parameters = parameters;
        this.totalNumberOfRows = totalNumberOfRows;
        this.hasAggregate = hasAggregate;
        this.layout = layout;
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

    public int getOrderColumn()
    {
        return orderColumn;
    }

    public String getOrderDir()
    {
        return orderDir;
    }

    public int getOffset()
    {
        return offset;
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

    public Object getLayout()
    {
        return layout;
    }

    @Override
    public String toString()
    {
        return "TablePresentation{" +
                "category='" + category + '\'' +
                ", page='" + page + '\'' +
                ", columns=" + columns +
                ", rows=" + rows +
                '}';
    }
}
