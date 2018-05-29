package com.developmentontheedge.be5.server.servlet;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.server.test.ServerBe5ProjectTest;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class TemplateFilterTest extends ServerBe5ProjectTest
{
    @Inject private TemplateFilter templateFilter;

    private ServletContext servletContext;

    private Response res;
    private Request req;
    private FilterChain filterChain;

    private String html = "<div th:text=\"${lang}\"></div>";

    private String resHtml = "<div>ru</div>";

    @Before
    public void setUp()
    {
        FilterConfig filterConfig = mock(FilterConfig.class);
        servletContext = mock(ServletContext.class);
        when(filterConfig.getServletContext()).thenReturn(servletContext);

        templateFilter.init(filterConfig);

        res = mock(Response.class);
        filterChain = mock(FilterChain.class);

        req = getSpyMockRequest("/");
    }

    @Test
    public void skip() throws IOException, ServletException
    {
        UserInfoHolder.setUserInfo(null);

        when(req.getContextPath()).thenReturn("/");

        templateFilter.filter(req, res, filterChain);

        assertEquals(RoleType.ROLE_GUEST, UserInfoHolder.getUserName());
        verify(filterChain, only()).doFilter(any(), any());
    }

    @Test
    public void test() throws IOException, ServletException
    {
        initGuest();

        when(req.getContextPath()).thenReturn("/");

        when(servletContext.getResourceAsStream("/WEB-INF/templates/index.html"))
                .thenReturn(new ByteArrayInputStream(html.getBytes()));

        templateFilter.filter(req, res, filterChain);

        verify(filterChain, never()).doFilter(any(), any());

        verify(res).sendHtml(eq(resHtml));
    }
}