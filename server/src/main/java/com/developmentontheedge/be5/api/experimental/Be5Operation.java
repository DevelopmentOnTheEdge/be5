package com.developmentontheedge.be5.api.experimental;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;

/**
 * Implement this interface by your legacy operation to get the service provider.
 * 
 * @see com.beanexplorer.enterprise.Operation
 */
public interface Be5Operation
{
    /**
     * It's called before the legacy operation's initializer.
     */
    void initialize(Request req, ServiceProvider serviceProvider);
}
