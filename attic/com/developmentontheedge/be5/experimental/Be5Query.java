package com.developmentontheedge.be5.api.experimental;

import com.developmentontheedge.be5.api.ServiceProvider;

/**
 * Implement this interface by your custom legacy query to get the service provider.
 * 
 * @see com.beanexplorer.enterprise.query.QueryIterator
 */
public interface Be5Query
{
    void initialize(ServiceProvider injector);
}
