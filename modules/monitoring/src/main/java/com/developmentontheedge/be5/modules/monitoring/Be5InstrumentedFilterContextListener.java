package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;

import static com.developmentontheedge.be5.modules.monitoring.Metrics.METRIC_REGISTRY;

public class Be5InstrumentedFilterContextListener extends InstrumentedFilterContextListener
{
    @Override
    protected MetricRegistry getMetricRegistry()
    {
        return METRIC_REGISTRY;
    }
}
