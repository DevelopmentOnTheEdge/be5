package com.developmentontheedge.be5.server.services.rememberme;

import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Optional;

public class RememberMeServicesImpl implements RememberMeServices
{
    private static final String REMEMBER_ME_KEY = "remember-me";
    private static final int TWO_WEEKS_S = 1209600;

    private final PersistentTokenRepository persistentTokenRepository;

    @Inject
    public RememberMeServicesImpl(PersistentTokenRepository persistentTokenRepository)
    {
        this.persistentTokenRepository = persistentTokenRepository;
    }

    @Override
    public String autoLogin(Request request, Response response)
    {
        Optional<Cookie> rememberMeCookie = getRememberMeCookie(request);
        if (rememberMeCookie.isPresent())
        {
            String id = rememberMeCookie.get().getValue();
            String username = persistentTokenRepository.getRememberedUser(id);
            if (username != null)
            {
                return username;
            }
        }
        return null;
    }

    @Override
    public void logout(Request request, Response response)
    {
        Optional<Cookie> cookie = getRememberMeCookie(request);
        if (cookie.isPresent())
        {
            String id = cookie.get().getValue();
            deleteRememberMeCookie(response, id);
        }
    }

    @Override
    public void rememberUser(Request request, Response response, String username)
    {
        String id = persistentTokenRepository.rememberUser(username);
        Cookie cookie = new Cookie(REMEMBER_ME_KEY, id);
        cookie.setPath("/");
        cookie.setMaxAge(TWO_WEEKS_S);
        response.addCookie(cookie);
    }

    private void deleteRememberMeCookie(Response response, String id)
    {
        persistentTokenRepository.removeRememberedUser(id);
        Cookie cookie = new Cookie(REMEMBER_ME_KEY, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Optional<Cookie> getRememberMeCookie(Request request)
    {
        Cookie[] cookies = request.getCookies();
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(REMEMBER_ME_KEY))
                .findFirst();
    }
}
