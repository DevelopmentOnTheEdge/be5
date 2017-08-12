package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class TableSupport implements TableBuilder
{
    protected Query query;
    protected Map<String, String> parametersMap;
    protected Request req;
    protected Injector injector;

    protected List<TableModel.ColumnModel> columns = new ArrayList<>();
    protected List<TableModel.RowModel> rows = new ArrayList<>();

    public final TableBuilder initialize(Query query, Map<String, String> parametersMap, Request req, Injector injector)
    {
        this.query = query;
        this.parametersMap = parametersMap;
        this.req = req;
        this.injector = injector;
        return this;
    }

    public List<TableModel.ColumnModel> columns(String firstName, String... columnNames)
    {
        List<TableModel.ColumnModel> columns = new ArrayList<>();
        columns.add(new TableModel.ColumnModel(firstName, firstName));
        if(columnNames != null)
        {
            for (String columnName : columnNames)
            {
                columns.add(new TableModel.ColumnModel(columnName, columnName));
            }
        }
        return Collections.unmodifiableList(columns);
    }

    public List<TableModel.CellModel> cells(Object firstContent, Object... contents)
    {
        List<TableModel.CellModel> columns = new ArrayList<>();
        columns.add(new TableModel.CellModel(firstContent, Collections.EMPTY_MAP));
        if(contents != null)
        {
            for (Object content : contents)
            {
                columns.add(new TableModel.CellModel(content, Collections.EMPTY_MAP));
            }
        }
        return Collections.unmodifiableList(columns);
    }

    public List<TableModel.CellModel> cells(TableModel.CellModel firstCell, TableModel.CellModel... cells)
    {
        List<TableModel.CellModel> columns = new ArrayList<>();
        columns.add(firstCell);
        Collections.addAll(columns, cells);
        return Collections.unmodifiableList(columns);
    }

    public TableModel.RowModel row(List<TableModel.CellModel> cells){
        return row("0", cells);
    }

    public TableModel.RowModel row(Integer id, List<TableModel.CellModel> cells){
        return row(id.toString(), cells);
    }

    public TableModel.RowModel row(String id, List<TableModel.CellModel> cells){
        return new TableModel.RowModel(id, cells);
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows){
        return new TableModel(columns, rows, false, (long) rows.size(), false);
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows, boolean selectable){
        return new TableModel(columns, rows, selectable, (long) rows.size(), false);
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows, boolean selectable, Long totalNumberOfRows, boolean hasAggregate){
        return new TableModel(columns, rows, selectable, totalNumberOfRows, hasAggregate);
    }

}
