package com.developmentontheedge.be5.model;

public class TableOperationPresentation
{

    public final String name;
    public final String title;
    public final String visibleWhen;
    public final boolean requiresConfirmation;
    public final boolean isClientSide;
    public final Action action;

    public TableOperationPresentation(String name, String title, String visibleWhen, boolean requiresConfirmation, boolean isClientSide, Action action)
    {
        this.name = name;
        this.title = title;
        this.visibleWhen = visibleWhen;
        this.requiresConfirmation = requiresConfirmation;
        this.isClientSide = isClientSide;
        this.action = action;
    }
    
}
