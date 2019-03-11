package com.developmentontheedge.be5.server.services.users;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.services.rememberme.RememberMeServices;
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
import java.util.stream.Collectors;


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

    public void saveUser(String username, boolean rememberMe)
    {
        List<String> availableRoles = roleService.getAvailableRoles(username);
        List<String> currentRoles = getAvailableCurrentRoles(roleService.getCurrentRoles(username), availableRoles);
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
            rememberMeService.onLoginSuccess(requestProvider.get(), responseProvider.get(), userName);
        }

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
        if (roles.isEmpty()) throw new IllegalArgumentException("There must be at least one role.");
        roleService.updateCurrentRoles(UserInfoHolder.getLoggedUser().getUserName(), roles);
        UserInfoHolder.getLoggedUser().setCurrentRoles(roles);
    }

    public void initUser(Request req, Response res)
    {
        String userName = rememberMeService.autoLogin(req, res);
        if (userName != null)
        {
            saveUser(userName, true);
        }
        else
        {
            initGuest();
        }
    }

    private void initGuest()
    {
        Request req = requestProvider.get();
        Objects.requireNonNull(req);

        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);

        saveUser(RoleType.ROLE_GUEST, roles, roles, req.getLocale(), req.getRemoteAddr(), false);
    }
}
