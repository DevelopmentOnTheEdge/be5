package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.operation.Operation;
import com.developmentontheedge.be5.server.FrontendActions;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.util.JsonUtils;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.util.Collections;
import java.util.Map;

public abstract class DownloadOperationSupport extends OperationSupport implements Operation
{
    @Override
    public void invoke(Object parameters) throws Exception
    {
        Map<String, Object> parametersMap = parameters != null ? ((DynamicPropertySet) parameters).asModifiableMap()
                : Collections.emptyMap();
        FrontendAction downloadOperationAction = FrontendActions.downloadOperation(
                getInfo().getEntityName(), context.getQueryName(), getInfo().getName(),
                context.getParams(), parametersMap
        );
        //String message = userAwareMeta.getLocalizedInfoMessage("Wait for the download to start.");
        //todo after add alertMessageAction
        Map<String, Object> layout = JsonUtils.getMapFromJson(getInfo().getModel().getLayout());
        if (parameters == null || "modalForm".equals(layout.get("type")))
        {
            setResultFinished(FrontendActions.closeMainModal(), downloadOperationAction);
        }
        else
        {
            setResultFinished(FrontendActions.goBack(), downloadOperationAction);
        }
    }

    public abstract void invokeWithResponse(Response res, Object parameters) throws Exception;
}
