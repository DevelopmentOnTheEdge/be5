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

    public List<TableModel.ColumnModel> getColumns(String firstName, String... columnNames)
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
        return columns;
    }

    public List<TableModel.CellModel> getCells(String firstContent, String... contents)
    {
        List<TableModel.CellModel> columns = new ArrayList<>();
        columns.add(new TableModel.CellModel(firstContent, Collections.EMPTY_MAP));
        if(contents != null)
        {
            for (String content : contents)
            {
                columns.add(new TableModel.CellModel(content, Collections.EMPTY_MAP));
            }
        }
        return columns;
    }

    public List<TableModel.CellModel> getCells(TableModel.CellModel firstCell, TableModel.CellModel... cells)
    {
        List<TableModel.CellModel> columns = new ArrayList<>();
        columns.add(firstCell);
        Collections.addAll(columns, cells);
        return columns;
    }

    public TableModel.RowModel getRow(List<TableModel.CellModel> cells){
        return getRow("0", cells);
    }

    public TableModel.RowModel getRow(Integer id, List<TableModel.CellModel> cells){
        return getRow(id.toString(), cells);
    }

    public TableModel.RowModel getRow(String id, List<TableModel.CellModel> cells){
        return new TableModel.RowModel(id, cells);
    }

    public TableModel getTable(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows){
        return new TableModel(columns, rows, false, (long) rows.size(), false);
    }

    public TableModel getTable(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows, boolean selectable, Long totalNumberOfRows, boolean hasAggregate){
        return new TableModel(columns, rows, selectable, totalNumberOfRows, hasAggregate);
    }
//
//    private final String emptyString = "";
//    private String nullToStr(String s){
//        if(s == null)return emptyString;
//        return s;
//    }
}
