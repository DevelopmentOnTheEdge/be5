package com.developmentontheedge.be5.server.model.table;

import java.util.List;
import java.util.Objects;

public class RowModel
{
    private final List<CellModel> cells;
    private final String id;

    /**
     * @param id can be null
     */
    public RowModel(String id, List<CellModel> cells)
    {
        Objects.requireNonNull(cells);
        this.id = id;
        this.cells = cells;
    }

    public List<CellModel> getCells()
    {
        return cells;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return "RowModel{" +
                "cells=" + cells +
                '}';
    }
}
