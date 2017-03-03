package com.developmentontheedge.be5.metadata.util;

import com.developmentontheedge.be5.metadata.exception.ProcessInterruptedException;

public interface ProcessController
{
    public void setOperationName(String name);
    
    public void setProgress(double progress) throws ProcessInterruptedException;
}
