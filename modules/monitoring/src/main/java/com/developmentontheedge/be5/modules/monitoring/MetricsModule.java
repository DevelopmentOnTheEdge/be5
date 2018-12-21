package com.developmentontheedge.be5.modules.monitoring;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.JvmAttributeGaugeSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.inject.servlet.ServletModule;
import org.marmelo.dropwizard.metrics.servlets.MetricsUIServlet;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;

import static com.developmentontheedge.be5.modules.monitoring.Metrics.METRIC_REGISTRY;

public class MetricsModule extends ServletModule
{
    private static final String PROP_METRIC_REG_JVM_MEMORY = "jvm.memory";
    private static final String PROP_METRIC_REG_JVM_GARBAGE = "jvm.garbage";
    private static final String PROP_METRIC_REG_JVM_THREADS = "jvm.threads";
    private static final String PROP_METRIC_REG_JVM_FILES = "jvm.files";
    private static final String PROP_METRIC_REG_JVM_BUFFERS = "jvm.buffers";
    private static final String PROP_METRIC_REG_JVM_ATTRIBUTE = "jvm.attribute";
    private static final String PROP_METRIC_REG_JVM_CLASSLOADER = "jvm.classloader";

    @Override
    protected void configureServlets()
    {
        bind(AdminServlet.class).asEagerSingleton();
        serve("/api/metrics/admin*").with(AdminServlet.class);

        bind(MetricsServlet.class).asEagerSingleton();
        serve("/metrics*").with(MetricsServlet.class);

        bind(MetricsUIServlet.class).asEagerSingleton();
        serve("/api/metrics/ui*").with(MetricsUIServlet.class);

        filter("/*").through(new InstrumentedFilter());


        Metrics.HEALTH_CHECKS.register("db", new DatabaseHealthCheck());

        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_MEMORY, new MemoryUsageGaugeSet());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_GARBAGE, new GarbageCollectorMetricSet());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_THREADS, new ThreadStatesGaugeSet());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_FILES, new FileDescriptorRatioGauge());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_ATTRIBUTE, new JvmAttributeGaugeSet());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_CLASSLOADER, new ClassLoadingGaugeSet());
        METRIC_REGISTRY.register(PROP_METRIC_REG_JVM_BUFFERS,
                new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));

        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);
        final InstrumentedAppender metrics = new InstrumentedAppender(METRIC_REGISTRY);
        metrics.setContext(root.getLoggerContext());
        metrics.start();
        root.addAppender(metrics);

//        final JmxReporter reporter = JmxReporter.forRegistry(METRIC_REGISTRY).build();
//        reporter.start();

//        String hostName = "192.168.66.29";
//        ZabbixSender zabbixSender = new ZabbixSender("https://zabbix.dote.ru/", 10051);
//        ZabbixReporter zabbixReporter = ZabbixReporter.forRegistry(METRIC_REGISTRY)
//                //.hostName(hostName)
//                .prefix("testBe5app.")
//                .build(zabbixSender);
//
//        zabbixReporter.start(5, TimeUnit.SECONDS);
    }
}
