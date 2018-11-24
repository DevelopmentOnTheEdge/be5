package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.web.Response;

public abstract class DownloadOperationSupport extends OperationSupport implements Operation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        FrontendAction downloadOperationAction = FrontendActions.downloadOperation(
                getInfo().getEntityName(), context.getQueryName(), getInfo().getName(),
                context.getOperationParams(), parameters
        );
        if (parameters == null)
        {
            setResultFinished(downloadOperationAction);
        }
        else
        {
            setResultFinished(FrontendActions.goBack(), downloadOperationAction);
        }
    }

    public abstract void invokeWithResponse(Response res, Object parameters);
}
