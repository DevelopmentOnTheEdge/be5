package com.developmentontheedge.be5.server.servlet.support;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.impl.RequestImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import org.junit.Before;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WebTestSupport
{
    HttpServletRequest rawRequest;
    HttpServletResponse rawResponse;
    Injector injector;

    @Before
    public void init() throws Exception
    {
        rawResponse = mock(HttpServletResponse.class);
        rawRequest = mock(HttpServletRequest.class);
        injector = Guice.createInjector(new TestWebModule());
    }

    class TestWebModule extends AbstractModule
    {
        @Provides
        Request provideUserInfo()
        {
            return new RequestImpl(rawRequest);
        }
    }
}
