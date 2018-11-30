package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.modules.core.services.RoleHelper;
import com.developmentontheedge.be5.server.helpers.MenuHelper;
import com.developmentontheedge.be5.server.helpers.UserHelper;
import com.developmentontheedge.be5.server.model.Action;
import com.developmentontheedge.be5.server.model.UserInfoModel;
import com.developmentontheedge.be5.web.Request;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    protected final DbService db;
    private final UserHelper userHelper;
    private final MenuHelper menuHelper;
    private final RoleHelper roleHelper;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public LoginServiceImpl(DbService db, UserHelper userHelper, MenuHelper menuHelper,
                            RoleHelper roleHelper, UserInfoProvider userInfoProvider)
    {
        this.db = db;
        this.userHelper = userHelper;
        this.menuHelper = menuHelper;
        this.roleHelper = roleHelper;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public UserInfoModel getUserInfoModel()
    {
        Action defaultAction = menuHelper.getDefaultAction();
        String defaultRouteCall = "";
        if (defaultAction == null)
        {
            log.severe("Default Action must not be null");
        }
        else
        {
            if (defaultAction.getName().equals("call"))
            {
                defaultRouteCall = defaultAction.getArg();
            }
            else
            {
                log.severe("Default Action type must be 'call'");
            }
        }
        UserInfo userInfo = userInfoProvider.get();
        return new UserInfoModel(
                !RoleType.ROLE_GUEST.equals(userInfo.getUserName()),
                userInfo.getUserName(),
                userInfo.getAvailableRoles(),
                userInfo.getCurrentRoles(),
                userInfo.getCreationTime().toInstant(),
                defaultRouteCall
        );
    }

    @Override
    public boolean loginCheck(String username, char[] rawPassword)
    {
        Objects.requireNonNull(username);
        Objects.requireNonNull(rawPassword);
        return db.countFrom("users WHERE user_name = ? AND user_pass = ?",
                username, new String(rawPassword)) == 1L;
    }

    @Override
    public String finalPassword(char[] password)
    {
        return new String(password);
    }

    @Override
    public void saveUser(String username, Request req)
    {
        List<String> availableRoles = roleHelper.getAvailableRoles(username);
        List<String> currentRoles = getAvailableCurrentRoles(roleHelper.getCurrentRoles(username), availableRoles);
        userHelper.saveUser(username, availableRoles, currentRoles, req.getLocale(), req.getRemoteAddr());
        log.fine("Login user: " + username);
    }

    @Override
    public void setCurrentRoles(List<String> roles)
    {
        Objects.requireNonNull(roles.get(0), "There must be at least one role.");
        roleHelper.updateCurrentRoles(userInfoProvider.get().getUserName(), roles);
        userInfoProvider.get().setCurrentRoles(roles);
    }

    @Override
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

}
