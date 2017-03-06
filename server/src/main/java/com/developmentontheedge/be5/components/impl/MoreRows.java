package com.developmentontheedge.be5.components.impl;

import java.util.List;

/**
 * Objects of this class should be serialized with Gson.
 * @author asko
 */
public class MoreRows {
    
    final int draw;
    final int recordsTotal;
    final int recordsFiltered;
    final List<List<Object>> data; // rows
    
    public MoreRows(int draw, int recordsTotal, int recordsFiltered, List<List<Object>> data)
    {
        this.draw = draw;
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }
    
}