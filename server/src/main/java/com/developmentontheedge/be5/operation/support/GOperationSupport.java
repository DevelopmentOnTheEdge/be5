package com.developmentontheedge.be5.operation.support;

import com.developmentontheedge.be5.model.beans.GDynamicPropertySetSupport;
import com.developmentontheedge.be5.operation.Operation;


public abstract class GOperationSupport extends OperationSupport implements Operation
{
    public GDynamicPropertySetSupport dps = new GDynamicPropertySetSupport();
}
