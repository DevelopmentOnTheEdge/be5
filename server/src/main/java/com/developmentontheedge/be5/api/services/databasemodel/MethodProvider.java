package com.developmentontheedge.be5.api.services.databasemodel;


public interface MethodProvider {

    Object invoke();
    
    Object invoke(Object... args);
    
}
