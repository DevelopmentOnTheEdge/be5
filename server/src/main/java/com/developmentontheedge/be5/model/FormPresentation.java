package com.developmentontheedge.be5.model;

import javax.json.JsonObject;
import java.util.Map;

public class FormPresentation
{

//    public final String entity;
//    public final String query;
//    public final String operation;
    public final String title;
    public final String selectedRows;
    public final JsonObject dps;
    public final Map<String, String> parameters;

    public FormPresentation(String title, String selectedRows, JsonObject dps, Map<String, String> parameters)
    {
        this.title = title;
        this.selectedRows = selectedRows;
        this.dps = dps;
        this.parameters = parameters;
    }

    public String getTitle()
    {
        return title;
    }

    public String getSelectedRows()
    {
        return selectedRows;
    }

    public JsonObject getDps()
    {
        return dps;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
    }
}
