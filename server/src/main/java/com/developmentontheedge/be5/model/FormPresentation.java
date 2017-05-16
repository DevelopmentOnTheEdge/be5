package com.developmentontheedge.be5.model;

import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.List;
import java.util.Map;

public class FormPresentation
{

//    public final String entity;
//    public final String query;
//    public final String operation;
    public final String title;
    public final String selectedRows;
    public final DynamicPropertySet dps;

    public FormPresentation(String title, String selectedRows, DynamicPropertySet dps)
    {
        this.title = title;
        this.selectedRows = selectedRows;
        this.dps = dps;
    }
}
