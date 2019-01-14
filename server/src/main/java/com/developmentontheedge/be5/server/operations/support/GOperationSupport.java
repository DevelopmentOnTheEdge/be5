package com.developmentontheedge.be5.server.operations.support;

import com.developmentontheedge.be5.groovy.GDynamicPropertySetSupport;
import com.developmentontheedge.be5.operation.Operation;


public abstract class GOperationSupport extends OperationSupport implements Operation
{
    public GDynamicPropertySetSupport params = new GDynamicPropertySetSupport();
}
