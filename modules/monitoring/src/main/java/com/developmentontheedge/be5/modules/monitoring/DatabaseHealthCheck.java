package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.health.HealthCheck;

public class DatabaseHealthCheck extends HealthCheck
{
    @Override
    protected Result check() throws Exception
    {
        return HealthCheck.Result.healthy();
    }
}
