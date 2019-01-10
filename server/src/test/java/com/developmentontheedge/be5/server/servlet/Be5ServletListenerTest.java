package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.base.lifecycle.LifecycleService;
import com.developmentontheedge.be5.base.lifecycle.LifecycleSupport;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.junit.Test;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static com.developmentontheedge.be5.base.lifecycle.State.STOPPED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class Be5ServletListenerTest
{
    private Injector injector = Guice.createInjector(new TestModule());

    @Test
    public void contextInitialized()
    {
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Injector.class.getName())).thenReturn(injector);
        new TestBe5ServletListener().contextInitialized(servletContextEvent);
        assertTrue(injector.getInstance(LifecycleService.class).isStarted());
    }

    @Test
    public void contextDestroyed()
    {
        ServletContextEvent servletContextEvent = mock(ServletContextEvent.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContextEvent.getServletContext()).thenReturn(servletContext);
        when(servletContext.getAttribute(Injector.class.getName())).thenReturn(injector);
        new TestBe5ServletListener().contextDestroyed(servletContextEvent);
        assertEquals(STOPPED, injector.getInstance(LifecycleService.class).getState());
    }

    @Test
    public void name()
    {
        assertEquals(Stage.DEVELOPMENT, new TestBe5ServletListener().getStage());
    }

    class TestBe5ServletListener extends Be5ServletListener
    {
        @Override
        protected Injector getInjector()
        {
            return injector;
        }
    }

    class TestModule extends AbstractModule
    {
        @Override
        protected void configure()
        {
            install(LifecycleSupport.getModule());
        }
    }
}
