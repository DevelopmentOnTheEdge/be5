package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.helpers.OperationHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.validation.Validator;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.databasemodel.impl.DatabaseModel;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.model.UserInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class TableBuilderSupport implements TableBuilder
{
    @Inject public DatabaseService databaseService;
    @Inject public DatabaseModel database;
    @Inject public SqlService db;
    @Inject public DpsHelper dpsHelper;
    @Inject public Meta meta;
    @Inject public OperationHelper helper;
    @Inject public Validator validator;

    protected Request request;
    protected Session session;
    protected UserInfo userInfo;

    protected Query query;
    protected Map<String, String> parametersMap;

    protected List<TableModel.ColumnModel> columns = new ArrayList<>();
    protected List<TableModel.RowModel> rows = new ArrayList<>();

    public TableBuilder initialize(Query query, Map<String, String> parametersMap)
    {
        this.query = query;
        this.parametersMap = parametersMap;

        this.request = UserInfoHolder.getRequest();
        this.session = UserInfoHolder.getSession();
        this.userInfo = UserInfoHolder.getUserInfo();

        return this;
    }

    public void addColumns(String firstName, String... columnNames)
    {
        columns.add(new TableModel.ColumnModel(firstName, firstName));
        if(columnNames != null)
        {
            for (String columnName : columnNames)
            {
                columns.add(new TableModel.ColumnModel(columnName, columnName));
            }
        }
    }

    public List<TableModel.CellModel> cells(Object firstContent, Object... contents)
    {
        List<TableModel.CellModel> columns = new ArrayList<>();
        columns.add(new TableModel.CellModel(firstContent, new HashMap<>() ));
        if(contents != null)
        {
            for (Object content : contents)
            {
                columns.add(new TableModel.CellModel(content, new HashMap<>() ));
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

    public void addRow(List<TableModel.CellModel> cells)
    {
        rows.add(new TableModel.RowModel("0", cells));
    }

    public void addRow(Integer id, List<TableModel.CellModel> cells)
    {
        rows.add(new TableModel.RowModel(id.toString(), cells));
    }

    public void addRow(String id, List<TableModel.CellModel> cells)
    {
        rows.add(new TableModel.RowModel(id, cells));
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows)
    {
        return new TableModel(columns, rows, false, (long) rows.size(), false);
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows, boolean selectable)
    {
        return new TableModel(columns, rows, selectable, (long) rows.size(), false);
    }

    public TableModel table(List<TableModel.ColumnModel> columns, List<TableModel.RowModel> rows,
                            boolean selectable, Long totalNumberOfRows, boolean hasAggregate)
    {
        return new TableModel(columns, rows, selectable, totalNumberOfRows, hasAggregate);
    }

}
