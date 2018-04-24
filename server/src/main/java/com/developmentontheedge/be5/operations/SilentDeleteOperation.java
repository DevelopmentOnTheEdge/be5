package com.developmentontheedge.be5.operations;


public class SilentDeleteOperation extends DeleteOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        redirectToTable(getInfo().getEntityName(), getContext().getQueryName(), getRedirectParams());
    }
}
