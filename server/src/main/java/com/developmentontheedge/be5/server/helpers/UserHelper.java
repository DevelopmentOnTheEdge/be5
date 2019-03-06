package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.services.InitUserService;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import com.google.inject.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.Cookie;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class UserHelper
{
    private static final Logger log = Logger.getLogger(UserHelper.class.getName());

    private static final String REMEMBER_ME = "remember-me";

    private final Meta meta;
    private final Stage stage;
    private final RoleHelper roleHelper;
    private final RememberUserHelper rememberUserHelper;
    private final InitUserService initUserService;
    private final Provider<Request> requestProvider;
    private final Provider<Response> responseProvider;

    @Inject
    public UserHelper(Meta meta, Stage stage, RoleHelper roleHelper, RememberUserHelper rememberUserHelper,
                      InitUserService initUserService, Provider<Request> requestProvider,
                      Provider<Response> responseProvider)
    {
        this.meta = meta;
        this.stage = stage;
        this.roleHelper = roleHelper;
        this.rememberUserHelper = rememberUserHelper;
        this.initUserService = initUserService;
        this.requestProvider = requestProvider;
        this.responseProvider = responseProvider;
    }

    public void saveUser(String username, boolean rememberMe)
    {
        List<String> availableRoles = roleHelper.getAvailableRoles(username);
        List<String> currentRoles = getAvailableCurrentRoles(roleHelper.getCurrentRoles(username), availableRoles);
        Request req = requestProvider.get();
        saveUser(username, availableRoles, currentRoles, req.getLocale(), req.getRemoteAddr(), rememberMe);
        log.fine("Login user: " + username); //TODO save to events
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles,
                             Locale locale, String remoteAddr, boolean rememberMe)
    {
        UserInfo ui;
        if (stage != Stage.PRODUCTION && ModuleLoader2.getDevRoles().size() > 0)
        {
            Set<String> devAvailableRoles = new LinkedHashSet<String>()
            {{
                addAll(availableRoles);
                addAll(ModuleLoader2.getDevRoles());
            }};
            Set<String> devCurrentRoles = new LinkedHashSet<String>()
            {{
                addAll(currentRoles);
                addAll(ModuleLoader2.getDevRoles());
            }};

            ui = new UserInfo(userName, devAvailableRoles, devCurrentRoles);

            log.info("Dev roles added - " + ModuleLoader2.getDevRoles().toString());
        }
        else
        {
            ui = new UserInfo(userName, availableRoles, currentRoles);
        }

        ui.setRemoteAddr(remoteAddr);
        ui.setLocale(meta.getLocale(locale));

        Session session = requestProvider.get().getSession();
        session.invalidate();

        session.set("remoteAddr", remoteAddr);
        session.set(SessionConstants.USER_INFO, ui);
        session.set(SessionConstants.CURRENT_USER, ui.getUserName());
        UserInfoHolder.setLoggedUser(ui);
        initUserService.initUser(userName);
        if (rememberMe)
        {
            rememberUser(userName);
        }

        return ui;
    }

    public void logout()
    {
        Session session = requestProvider.get().getSession();
        session.invalidate();
        Optional<Cookie> cookie = getRememberMeCookie();
        if (cookie.isPresent())
        {
            String id = cookie.get().getValue();
            deleteRememberMeCookie(id);
        }
        initGuest();
    }

    public List<String> getAvailableCurrentRoles(List<String> roles, List<String> availableRoles)
    {
        List<String> newRoles = roles.stream()
                .filter(availableRoles::contains)
                .collect(Collectors.toList());
        if (newRoles.size() > 0)
        {
            return newRoles;
        }
        else
        {
            return Collections.singletonList(availableRoles.get(0));
        }
    }

    public void setCurrentRoles(List<String> roles)
    {
        Objects.requireNonNull(roles.get(0), "There must be at least one role.");
        roleHelper.updateCurrentRoles(UserInfoHolder.getLoggedUser().getUserName(), roles);
        UserInfoHolder.getLoggedUser().setCurrentRoles(roles);
    }

    public void initUser()
    {
        if (!loginRememberedUser())
        {
            initGuest();
        }
    }

    private boolean loginRememberedUser()
    {
        Optional<Cookie> rememberMeCookie = getRememberMeCookie();
        if (rememberMeCookie.isPresent())
        {
            String id = rememberMeCookie.get().getValue();
            String username = rememberUserHelper.getRememberedUser(id);
            if (username != null)
            {
                saveUser(username, true);
                return true;
            }
        }
        return false;
    }

    private void initGuest()
    {
        Request req = requestProvider.get();
        Objects.requireNonNull(req);

        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);

        saveUser(RoleType.ROLE_GUEST, roles, roles, req.getLocale(), req.getRemoteAddr(), false);
    }

    private void rememberUser(String username)
    {
        String id = rememberUserHelper.rememberUser(username);
        Cookie cookie = new Cookie(REMEMBER_ME, id);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24 * 30); // valid for 30 days
        responseProvider.get().addCookie(cookie);
    }

    private void deleteRememberMeCookie(String id)
    {
        rememberUserHelper.removeRememberedUser(id);
        Cookie cookie = new Cookie(REMEMBER_ME, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        responseProvider.get().addCookie(cookie);
    }

    private Optional<Cookie> getRememberMeCookie()
    {
        Cookie[] cookies = requestProvider.get().getCookies();
        return Arrays.stream(cookies)
                .filter(c -> c.getName().equals(REMEMBER_ME))
                .findFirst();
    }
}
