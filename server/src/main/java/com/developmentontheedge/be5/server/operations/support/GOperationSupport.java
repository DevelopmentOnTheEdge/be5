package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.operation.model.Operation;
import com.developmentontheedge.be5.server.model.beans.GDynamicPropertySetSupport;


public abstract class GOperationSupport extends OperationSupport implements Operation
{
    public GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport();
}
