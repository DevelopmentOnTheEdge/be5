package com.developmentontheedge.be5.modules.core.operations.system;

import com.developmentontheedge.be5.base.cache.Be5Caches;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;

import javax.inject.Inject;


public class ClearAllCaches extends OperationSupport
{
    @Inject
    private Be5Caches be5Caches;

    @Override
    public void invoke(Object parameters) throws Exception
    {
        be5Caches.clearAll();
    }
}
