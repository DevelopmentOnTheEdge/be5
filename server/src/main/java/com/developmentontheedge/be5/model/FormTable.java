package com.developmentontheedge.be5.model;

@Deprecated
public class FormTable
{

    public final FormPresentation form;
    public final TablePresentation table;
    
    public FormTable(FormPresentation form, TablePresentation table)
    {
        this.form = form;
        this.table = table;
    }

    public FormPresentation getForm()
    {
        return form;
    }

    public TablePresentation getTable()
    {
        return table;
    }
}
