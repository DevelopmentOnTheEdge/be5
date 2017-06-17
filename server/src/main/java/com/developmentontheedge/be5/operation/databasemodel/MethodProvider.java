package com.developmentontheedge.be5.operation.databasemodel;


public interface MethodProvider {

    Object invoke();
    
    Object invoke(Object... args);
    
}
