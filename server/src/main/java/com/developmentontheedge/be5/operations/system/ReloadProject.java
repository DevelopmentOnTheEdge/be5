package com.developmentontheedge.be5.operations.system;

import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.operations.support.OperationSupport;

import javax.inject.Inject;


public class ReloadProject extends OperationSupport
{
    @Inject private ProjectProvider projectProvider;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        projectProvider.reloadProject();
    }
}
