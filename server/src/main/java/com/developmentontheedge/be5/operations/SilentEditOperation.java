package com.developmentontheedge.be5.operations;


public class SilentEditOperation extends EditOperation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        super.invoke(parameters);

        redirectToTable(getInfo().getEntityName(), getContext().getQueryName(), getRedirectParams());
    }
}
