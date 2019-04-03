package com.developmentontheedge.be5.server.authentication;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.authentication.rememberme.RememberMeServices;
import com.developmentontheedge.be5.server.services.events.LogBe5Event;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Response;
import com.developmentontheedge.be5.web.Session;
import com.google.inject.Stage;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;


public class UserService
{
    private static final Logger log = Logger.getLogger(UserService.class.getName());

    private final Meta meta;
    private final Stage stage;
    private final RoleService roleService;
    private final RememberMeServices rememberMeService;
    private final InitUserService initUserService;
    private final Provider<Request> requestProvider;
    private final Provider<Response> responseProvider;

    @Inject
    public UserService(Meta meta, Stage stage, RoleService roleService, RememberMeServices rememberMeService,
                       InitUserService initUserService, Provider<Request> requestProvider,
                       Provider<Response> responseProvider)
    {
        this.meta = meta;
        this.stage = stage;
        this.roleService = roleService;
        this.rememberMeService = rememberMeService;
        this.initUserService = initUserService;
        this.requestProvider = requestProvider;
        this.responseProvider = responseProvider;
    }

    public UserInfo saveUser(String userName, boolean rememberMe)
    {
        List<String> availableRoles = roleService.getAvailableRoles(userName);
        List<String> currentRoles = roleService.getCurrentRoles(userName);
        return saveUser(userName, availableRoles, currentRoles, rememberMe);
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles, boolean rememberMe)
    {
        Request req = requestProvider.get();
        return saveUser(userName, availableRoles, currentRoles, req.getLocale(), req.getRemoteAddr(), rememberMe);
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles,
                             Locale locale, String remoteAddr, boolean rememberMe)
    {
        UserInfo ui = createUserInfo(userName, availableRoles, currentRoles, locale, remoteAddr);

        Session session = requestProvider.get().getSession();
        session.invalidate();

        session.set("remoteAddr", remoteAddr);
        session.set(SessionConstants.USER_INFO, ui);
        session.set(SessionConstants.CURRENT_USER, ui.getUserName());
        UserInfoHolder.setLoggedUser(ui);
        initUserService.initUser(userName);
        if (rememberMe)
        {
            rememberMeService.onLoginSuccess(requestProvider.get(), responseProvider.get(), userName);
        }

        log.fine("Login user: " + userName);
        return ui;
    }

    private UserInfo createUserInfo(String userName, List<String> availableRoles, List<String> currentRoles,
                                    Locale locale, String remoteAddr)
    {
        UserInfo ui;
        if (stage != Stage.PRODUCTION && ModuleLoader2.getDevRoles().size() > 0)
        {
            Set<String> devAvailableRoles = new LinkedHashSet<>(availableRoles);
            Set<String> devCurrentRoles = new LinkedHashSet<>(currentRoles);

            devAvailableRoles.addAll(ModuleLoader2.getDevRoles());
            devCurrentRoles.addAll(ModuleLoader2.getDevRoles());

            log.info("Dev roles added - " + ModuleLoader2.getDevRoles().toString());
            ui = new UserInfo(userName, devAvailableRoles, devCurrentRoles);
        }
        else
        {
            ui = new UserInfo(userName, availableRoles, currentRoles);
        }

        ui.setRemoteAddr(remoteAddr);
        ui.setLocale(meta.getLocale(locale));
        return ui;
    }

    public void logout(Request req, Response res)
    {
        Session session = req.getSession(false);
        String username = session != null ? (String) session.get(SessionConstants.CURRENT_USER) : null;
        if (username != null)
        {
            rememberMeService.logout(req, res, username);
        }
        if (session != null) session.invalidate();
        initGuest();
    }

    public void setCurrentRoles(List<String> roles)
    {
        List<String> availableCurrentRoles = roleService.getAvailableCurrentRoles(roles,
                UserInfoHolder.getLoggedUser().getAvailableRoles());
        if (availableCurrentRoles.isEmpty()) throw new IllegalArgumentException("There must be at least one role.");
        roleService.updateCurrentRoles(UserInfoHolder.getLoggedUser().getUserName(), availableCurrentRoles);
        UserInfoHolder.getLoggedUser().setCurrentRoles(availableCurrentRoles);
    }

    public void initUser(Request req, Response res)
    {
        String userName = rememberMeService.autoLogin(req, res);
        if (userName != null)
        {
            saveAutoLoginUser(userName);
        }
        else
        {
            initGuest();
        }
    }

    @LogBe5Event
    void saveAutoLoginUser(String userName)
    {
        saveUser(userName, true);
    }

    private void initGuest()
    {
        Request req = requestProvider.get();
        Objects.requireNonNull(req);

        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);

        saveUser(RoleType.ROLE_GUEST, roles, roles, req.getLocale(), req.getRemoteAddr(), false);
    }
}
