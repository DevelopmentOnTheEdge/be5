package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

public class Be5HealthCheckServletContextListener extends HealthCheckServlet.ContextListener
{
    @Override
    protected HealthCheckRegistry getHealthCheckRegistry()
    {
        return Metrics.HEALTH_CHECKS;
    }
}
