package com.developmentontheedge.be5.modules.monitoring;

import com.codahale.metrics.Counter;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class MetricsSessionListener implements HttpSessionListener
{
    private final Counter counterOfActiveSessions;

    public MetricsSessionListener()
    {
        super();
        counterOfActiveSessions = Metrics.METRIC_REGISTRY.counter("web.sessions.active.count");
    }

    public void sessionCreated(final HttpSessionEvent event)
    {
        counterOfActiveSessions.inc();
    }

    public void sessionDestroyed(final HttpSessionEvent event)
    {
        counterOfActiveSessions.dec();
    }
}
