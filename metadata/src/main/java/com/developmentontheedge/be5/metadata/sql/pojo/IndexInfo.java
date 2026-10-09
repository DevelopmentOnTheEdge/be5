package com.developmentontheedge.be5.metadata.sql.pojo;

import java.util.ArrayList;
import java.util.List;

public class IndexInfo
{
    private String name;
    private boolean unique;
    private String method;
    private String operatorClass;
    private String options;
    private final List<String> columns = new ArrayList<>();

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isUnique()
    {
        return unique;
    }

    public void setUnique(boolean unique)
    {
        this.unique = unique;
    }

    public String getMethod()
    {
        return method;
    }

    public void setMethod(String method)
    {
        this.method = method;
    }

    public String getOperatorClass()
    {
        return operatorClass;
    }

    public void setOperatorClass(String operatorClass)
    {
        this.operatorClass = operatorClass;
    }

    public String getOptions()
    {
        return options;
    }

    public void setOptions(String options)
    {
        this.options = options;
    }

    public void addColumn(String col)
    {
        columns.add(col);
    }

    public List<String> getColumns()
    {
        return columns;
    }
}
