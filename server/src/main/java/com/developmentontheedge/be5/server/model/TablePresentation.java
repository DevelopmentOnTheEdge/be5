package com.developmentontheedge.be5.server.model;

import com.developmentontheedge.be5.server.model.table.ColumnModel;

import java.util.List;
import java.util.Map;

public class TablePresentation
{
    private final String title;
    private final String category;
    private final String page;
    private final boolean selectable;
    private final List<ColumnModel> columns;
    private final List rows;

    private final int offset;
    private final int orderColumn;
    private final String orderDir;
    private final int length;

    private final Map<String, Object> parameters;
    private final Long totalNumberOfRows;
    private final Object layout;
    private final String messageWhenEmpty;

    public TablePresentation(
            String title,
            String category,
            String page,
            boolean selectable,
            List<ColumnModel> columns,
            List rows,
            int orderColumn,
            String orderDir,
            int offset,
            int length,
            Map<String, Object> parameters,
            Long totalNumberOfRows,
            Object layout,
            String messageWhenEmpty)
    {
        this.title = title;
        this.category = category;
        this.page = page;
        this.selectable = selectable;
        this.columns = columns;
        this.rows = rows;
        this.orderColumn = orderColumn;
        this.orderDir = orderDir;
        this.offset = offset;
        this.length = length;
        this.parameters = parameters;
        this.totalNumberOfRows = totalNumberOfRows;
        this.layout = layout;
        this.messageWhenEmpty = messageWhenEmpty;
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

    public boolean isSelectable()
    {
        return selectable;
    }

    public List<ColumnModel> getColumns()
    {
        return columns;
    }

    public List getRows()
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

    public Map<String, Object> getParameters()
    {
        return parameters;
    }

    public Long getTotalNumberOfRows()
    {
        return totalNumberOfRows;
    }

    public Object getLayout()
    {
        return layout;
    }

    public String getMessageWhenEmpty()
    {
        return messageWhenEmpty;
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
