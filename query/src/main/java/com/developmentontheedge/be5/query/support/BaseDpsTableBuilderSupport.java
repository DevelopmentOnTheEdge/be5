package com.developmentontheedge.be5.query.support;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.DpsTableBuilder;
import com.developmentontheedge.be5.query.model.CellModel;
import com.developmentontheedge.be5.query.model.ColumnModel;
import com.developmentontheedge.be5.query.model.RowModel;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public abstract class BaseDpsTableBuilderSupport implements DpsTableBuilder
{
    protected Query query;
    protected Map<String, Object> parameters;

    protected List<String> columns = new ArrayList<>();
    protected List<DynamicPropertySet> rows = new ArrayList<>();

    public DpsTableBuilder initialize(Query query, Map<String, Object> parameters)
    {
        this.query = query;
        this.parameters = parameters;

        return this;
    }

    public void addColumns(String firstName, String... columnNames)
    {
        columns.add(firstName);
        if (columnNames != null)
        {
            columns.addAll(Arrays.asList(columnNames));
        }
    }

    public List<DynamicProperty> cells(Object cell, Object... otherCells)
    {
        List<Object> allCells = new ArrayList<>();
        allCells.add(cell);
        allCells.addAll(Arrays.asList(otherCells));

        List<DynamicProperty> cells = new ArrayList<>();

        for (int i = 0; i < allCells.size(); i++)
        {
            if (allCells.get(i) instanceof DynamicProperty)
            {
                cells.add((DynamicProperty) allCells.get(i));
            }
            else
            {
                cells.add(new DynamicProperty(columns.get(i), String.class, allCells.get(i)));
            }
        }

        return Collections.unmodifiableList(cells);
    }

    public List<CellModel> cells(CellModel firstCell, CellModel... cells)
    {
        List<CellModel> columns = new ArrayList<>();
        columns.add(firstCell);
        Collections.addAll(columns, cells);
        return Collections.unmodifiableList(columns);
    }

    public CellModel cell(Object content)
    {
        return new CellModel(content);
    }

    public void addRow(List<DynamicProperty> cells)
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        cells.forEach(dps::add);
        rows.add(dps);
    }

    public void addRow(Integer id, List<DynamicProperty> cells)
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("___ID", String.class, id.toString()));
        cells.forEach(dps::add);
        rows.add(dps);
    }

    public void addRow(String id, List<DynamicProperty> cells)
    {
        DynamicPropertySetSupport dps = new DynamicPropertySetSupport();
        dps.add(new DynamicProperty("___ID", String.class, id));
        cells.forEach(dps::add);
        rows.add(dps);
    }

    public TableModel table()
    {
        return getSimpleTable(false, (long) rows.size(), false);
    }

    public TableModel table(boolean selectable)
    {
        return getSimpleTable(selectable, (long) rows.size(), false);
    }

    public TableModel table(boolean selectable, Long totalNumberOfRows, boolean hasAggregate)
    {
        return getSimpleTable(selectable, totalNumberOfRows, hasAggregate);
    }

    private TableModel getSimpleTable(boolean selectable, Long totalNumberOfRows, boolean hasAggregate)
    {
        List<ColumnModel> columns = new ArrayList<>();
        List<RowModel> rows = new ArrayList<>();
        return new TableModel(columns, rows, selectable, totalNumberOfRows, hasAggregate,
                0, Integer.MAX_VALUE, -1, "asc");
    }
}
