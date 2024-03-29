package com.developmentontheedge.be5.database.impl;

import com.developmentontheedge.be5.database.ConnectionService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import javax.inject.Inject;

public class TransactionInterceptor implements MethodInterceptor
{
    @Inject
    private ConnectionService connectionService;

    @Override
    public Object invoke(MethodInvocation invocation)
    {
        return connectionService.inTransaction(conn -> invocation.proceed());
        /*
        try
        {
            return invocation.proceed();
        }
        catch( Throwable t )
        {
            throw new RuntimeException( t );
        }
        */
    }
}
