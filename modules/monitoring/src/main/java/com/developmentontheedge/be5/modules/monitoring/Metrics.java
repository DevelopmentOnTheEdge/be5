package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;

public class Metrics
{
    public static final HealthCheckRegistry HEALTH_CHECKS = new HealthCheckRegistry();
    public static final MetricRegistry METRIC_REGISTRY = new MetricRegistry();
}
