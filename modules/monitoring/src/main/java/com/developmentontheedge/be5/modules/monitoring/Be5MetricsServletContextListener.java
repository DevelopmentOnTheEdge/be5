package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

public class Be5MetricsServletContextListener extends MetricsServlet.ContextListener
{
    @Override
    protected MetricRegistry getMetricRegistry()
    {
        return Metrics.METRIC_REGISTRY;
    }
}
