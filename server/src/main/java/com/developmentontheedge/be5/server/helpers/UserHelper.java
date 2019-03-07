package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.security.UserInfo;
import com.developmentontheedge.be5.security.UserInfoHolder;
import com.developmentontheedge.be5.server.SessionConstants;
import com.developmentontheedge.be5.server.services.InitUserService;
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


public class UserHelper
{
    private static final Logger log = Logger.getLogger(UserHelper.class.getName());

    private final Meta meta;
    private final Stage stage;
    private final RoleHelper roleHelper;
    private final RememberMeServices rememberMeService;
    private final InitUserService initUserService;
    private final Provider<Request> requestProvider;
    private final Provider<Response> responseProvider;

    @Inject
    public UserHelper(Meta meta, Stage stage, RoleHelper roleHelper, RememberMeServices rememberMeService,
                      InitUserService initUserService, Provider<Request> requestProvider,
                      Provider<Response> responseProvider)
    {
        this.meta = meta;
        this.stage = stage;
        this.roleHelper = roleHelper;
        this.rememberMeService = rememberMeService;
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
            rememberMeService.rememberUser(requestProvider.get(), responseProvider.get(), userName);
        }

        return ui;
    }

    public void logout()
    {
        Session session = requestProvider.get().getSession();
        session.invalidate();
        rememberMeService.logout(requestProvider.get(), responseProvider.get());
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
        roleHelper.updateCurrentRoles(UserInfoHolder.getLoggedUser().getUserName(), roles);
        UserInfoHolder.getLoggedUser().setCurrentRoles(roles);
    }

    public void initUser()
    {
        String userName = rememberMeService.autoLogin(requestProvider.get(), responseProvider.get());
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
