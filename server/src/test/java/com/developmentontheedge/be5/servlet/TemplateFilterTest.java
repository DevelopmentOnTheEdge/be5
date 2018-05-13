package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateFilterTest extends ServerBe5ProjectTest
{
    private UserAwareMeta userAwareMeta;
    private UserHelper userHelper;

    private TemplateFilter templateFilter;

    private FilterConfig filterConfig;
    private ServletContext servletContext;

    private Response res;
    private Request req;
    private FilterChain filterChain;

    private String html = "<div th:text=\"${lang}\"></div>";

    private String resHtml = "<div>ru</div>";

    @Before
    public void setUp()
    {
        userAwareMeta = mock(UserAwareMeta.class);
        userHelper = mock(UserHelper.class);

        templateFilter = new TemplateFilter(userAwareMeta, userHelper);

        filterConfig = mock(FilterConfig.class);
        servletContext = mock(ServletContext.class);
        when(filterConfig.getServletContext()).thenReturn(servletContext);

        templateFilter.init(filterConfig);

        res = mock(Response.class);
        filterChain = mock(FilterChain.class);

        req = getMockRequest("/");
    }

    @Test
    public void skip() throws IOException, ServletException
    {
        UserInfoHolder.setUserInfo(null);

        when(req.getContextPath()).thenReturn("/");

        templateFilter.filter(req, res, filterChain);

        verify(userHelper, only()).initGuest(req);
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