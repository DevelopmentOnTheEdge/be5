package com.developmentontheedge.be5.server.authentication.rememberme;

import com.developmentontheedge.be5.util.DateUtils;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Mikhail Stryzhonok.
 */
public class ThrottlingRememberMeServiceTest
{
    private static final String PRESENTED_SERIES = "61ikbvB7Nd1Wk3jDXgN/TQ==";
    private static final String PRESENTED_TOKEN = "FGGNNSS0KoIg7zO9+VlSaw==";

    @Mock
    private PersistentTokenRepository tokenRepository;

    @Spy
    private ThrottlingRememberMeService services;

    private PersistentRememberMeToken token = new PersistentRememberMeToken(
            "user", PRESENTED_SERIES, PRESENTED_TOKEN, DateUtils.currentTimestamp());

    @Before
    public void init()
    {
        services = new ThrottlingRememberMeService(null, null, null);
        initMocks(this);

        doReturn("joeuser").when(services).loginWithSpringSecurity(any(String[].class), any(Request.class), any(Response.class));
        doNothing().when(services).rewriteCookie(eq(token), any(Request.class), any(Response.class));

        when(tokenRepository.getTokenForSeries(PRESENTED_SERIES)).thenReturn(token);

        services.setTokenRepository(tokenRepository);
    }

    @Test
    public void loginWithSpringSecurityShouldBeCalledOnceWhenTokenValid()
    {
        services.setCachedTokenValidityTime(5000);
        services.processAutoLoginCookie(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}, null, null);
        services.processAutoLoginCookie(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}, null, null);
        verify(services, times(1)).loginWithSpringSecurity(eq(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}),
                any(Request.class), any(Response.class));
    }

    @Test
    public void loginWithSpringSecurityShouldBeCalledTwoTimesWhenTokenInvalid() throws Exception
    {
        services.setCachedTokenValidityTime(1000);
        services.processAutoLoginCookie(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}, null, null);
        Thread.sleep(1500);
        services.processAutoLoginCookie(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}, null, null);
        verify(services, times(2)).loginWithSpringSecurity(eq(new String[]{PRESENTED_SERIES, PRESENTED_TOKEN}),
                any(Request.class), any(Response.class));
    }
}
