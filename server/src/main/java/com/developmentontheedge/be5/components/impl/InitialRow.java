package com.developmentontheedge.be5.components.impl;

import java.util.List;

public class InitialRow {
    
    public final String id;
    public final List<Object> cells;
    
    public InitialRow(String id, List<Object> cells)
    {
        this.id = id;
        this.cells = cells;
    }
    
}