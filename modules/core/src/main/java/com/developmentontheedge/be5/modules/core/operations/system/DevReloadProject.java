package com.developmentontheedge.be5.modules.core.operations.system;

import com.developmentontheedge.be5.base.meta.ProjectProvider;
import com.developmentontheedge.be5.operation.model.OperationResult;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.google.inject.Stage;

import javax.inject.Inject;


public class DevReloadProject extends OperationSupport
{
    @Inject
    private ProjectProvider projectProvider;

    @Inject
    private Stage stage;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        if (stage == Stage.DEVELOPMENT)
        {
            projectProvider.reloadProject();
        }
        else
        {
            setResult(OperationResult.error("Only in DEVELOPMENT stage"));
        }
    }
}
