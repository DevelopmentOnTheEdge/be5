package com.developmentontheedge.be5.inject;

/**
 * This class can be used to initialize module state.
 * Initializers can be configurable.
 * 
 * @see Configurable
 * @author asko
 */
public interface Initializer
{

    /**
     * Called once when the main servlet is loaded.
     * 
     * @see InitializerContext
     * @see Injector
     */
    void initialize(InitializerContext context, Injector injector);
    
}
