package com.developmentontheedge.be5.api.experimental;

import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.components.LegacyServlet;

/**
 * Implement this interface by your legacy servlet to get the service provider.
 * 
 * @see LegacyServlet
 * @author asko
 */
public interface Be5Servlet
{
    void initialize(ServiceProvider serviceProvider);
}
