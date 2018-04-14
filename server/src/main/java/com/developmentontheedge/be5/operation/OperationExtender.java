package com.developmentontheedge.be5.operation;


public interface OperationExtender
{
    boolean skipInvoke(Operation op, Object parameters);

    void preInvoke(Operation op, Object parameters) throws Exception;

    void postInvoke(Operation op, Object parameters) throws Exception;
}
