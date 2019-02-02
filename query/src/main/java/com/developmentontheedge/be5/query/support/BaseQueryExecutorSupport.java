package com.developmentontheedge.be5.query.support;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.util.DynamicPropertyMeta;
import com.developmentontheedge.beans.DynamicProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.developmentontheedge.be5.metadata.DatabaseConstants.ID_COLUMN_LABEL;


public abstract class BaseQueryExecutorSupport extends AbstractQueryExecutor implements QueryExecutor
{
    protected Query query;
    protected Map<String, Object> parameters;
    private boolean selectable;

    protected List<String> columns = new ArrayList<>();
    protected List<QRec> properties = new ArrayList<>();

    public void initialize(Query query, Map<String, Object> parameters)
    {
        this.query = query;
        this.parameters = parameters;
    }

    public void addColumns(String firstName, String... columnNames)
    {
        columns.add(firstName);
        if (columnNames != null)
        {
            Collections.addAll(columns, columnNames);
        }
    }

    public List<DynamicProperty> cells(Object cell, Object... otherCells)
    {
        List<Object> allCells = new ArrayList<>();
        allCells.add(cell);
        Collections.addAll(allCells, otherCells);

        List<DynamicProperty> cells = new ArrayList<>();

        for (int i = 0; i < allCells.size(); i++)
        {
            if (allCells.get(i) instanceof DynamicProperty)
            {
                cells.add((DynamicProperty) allCells.get(i));
            }
            else if (allCells.get(i) instanceof Cell)
            {
                Cell item = (Cell) allCells.get(i);
                DynamicProperty property = new DynamicProperty(columns.get(i), String.class, item.content);
                DynamicPropertyMeta.set(property, item.options);
                cells.add(property);
            }
            else
            {
                cells.add(new DynamicProperty(columns.get(i), String.class, allCells.get(i)));
            }
        }

        return Collections.unmodifiableList(cells);
    }

    public List<DynamicProperty> cells(Cell firstCell, Cell... otherCells)
    {
        List<Cell> cells = new ArrayList<>();
        cells.add(firstCell);
        Collections.addAll(cells, otherCells);
        List<DynamicProperty> list = new ArrayList<>();
        for (int i = 0; i < cells.size(); i++)
        {
            DynamicProperty property = new DynamicProperty(columns.get(i), String.class, cells.get(i).content);
            DynamicPropertyMeta.set(property, cells.get(i).options);
            list.add(property);
        }
        return list;
    }

    public Cell cell(Object content)
    {
        return new Cell(content);
    }

    public void addRow(List<DynamicProperty> cells)
    {
        QRec dps = new QRec();
        cells.forEach(dps::add);
        properties.add(dps);
    }

    public void addRow(Long id, List<DynamicProperty> cells)
    {
        QRec dps = new QRec();
        dps.add(new DynamicProperty(ID_COLUMN_LABEL, String.class, id.toString()));
        cells.forEach(dps::add);
        properties.add(dps);
    }

    public void addRow(String id, List<DynamicProperty> cells)
    {
        QRec dps = new QRec();
        dps.add(new DynamicProperty(ID_COLUMN_LABEL, String.class, id));
        cells.forEach(dps::add);
        properties.add(dps);
    }

    public List<QRec> table()
    {
        return getSimpleTable(false, (long) properties.size());
    }

    public List<QRec> table(boolean selectable)
    {
        return getSimpleTable(selectable, (long) properties.size());
    }

    public List<QRec> table(boolean selectable, Long totalNumberOfRows)
    {
        return getSimpleTable(selectable, totalNumberOfRows);
    }

    private List<QRec> getSimpleTable(boolean selectable, Long totalNumberOfRows)
    {
        this.selectable = selectable;
        return properties;
    }

    @Override
    public Boolean isSelectable()
    {
        return selectable;
    }

    @Override
    public long count()
    {
        return getLimit();
    }

    public Map<String, Object> getParameters()
    {
        return parameters;
    }
}
