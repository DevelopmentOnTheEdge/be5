package com.developmentontheedge.be5.operation.databasemodel;


public interface MethodProvider {

    public Object invoke();
    
    public Object invoke(Object... args);
    
}
