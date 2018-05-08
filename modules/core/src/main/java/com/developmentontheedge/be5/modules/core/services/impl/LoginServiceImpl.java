package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.helpers.MenuHelper;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.helpers.UserHelper;
import com.developmentontheedge.be5.api.services.CoreUtils;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.DatabaseConstants;
import com.developmentontheedge.be5.metadata.MetadataUtils;
import com.developmentontheedge.be5.model.Action;
import com.developmentontheedge.be5.modules.core.model.UserInfoModel;
import com.developmentontheedge.be5.modules.core.services.LoginService;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    private final SqlService db;
    private final UserHelper userHelper;
    private final CoreUtils coreUtils;
    private final MenuHelper menuHelper;

    @Inject
    public LoginServiceImpl(SqlService db, UserHelper userHelper, CoreUtils coreUtils, MenuHelper menuHelper)
    {
        this.db = db;
        this.userHelper = userHelper;
        this.coreUtils = coreUtils;
        this.menuHelper = menuHelper;
    }

    @Override
    public UserInfoModel getUserInfoModel()
    {
        Action defaultAction = menuHelper.getDefaultAction();
        String defaultRouteCall = "";

        if(defaultAction == null)
        {
            log.severe("Default Action must not be null");
        }
        else
        {
            if(defaultAction.getName().equals("call")){
                defaultRouteCall = defaultAction.getArg();
            }else{
                log.severe("Default Action type must be 'call'");
            }
        }

        return new UserInfoModel(
                UserInfoHolder.isLoggedIn(),
                UserInfoHolder.getUserName(),
                UserInfoHolder.getAvailableRoles(),
                UserInfoHolder.getCurrentRoles(),
                UserInfoHolder.getUserInfo().getCreationTime().toInstant(),
                defaultRouteCall
        );
    }

    public boolean loginCheck(String username, String password)
    {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        String sql = "SELECT COUNT(user_name) FROM users WHERE user_name = ? AND user_pass = ?";

        return db.getLong(sql, username, password) == 1L;
    }

    private List<String> selectAvailableRoles(String username)
    {
        return db.selectList("SELECT role_name FROM user_roles WHERE user_name = ?",
                    rs -> rs.getString(1), username);
    }

    @Override
    public void saveUser(String username, Request req)
    {
        List<String> availableRoles = selectAvailableRoles(username);

        String savedRoles = coreUtils.getUserSetting(username, DatabaseConstants.CURRENT_ROLE_LIST);

        List<String> currentRoles;
        if(savedRoles != null)
        {
            currentRoles = parseRoles(savedRoles);
        }
        else
        {
            currentRoles = availableRoles;
        }

        userHelper.saveUser(username, availableRoles, currentRoles,
                req.getLocale(), req.getRemoteAddr(), req.getSession());

        log.fine("Login user: " + username);
    }

    @Override
    public void setCurrentRoles(List<String> roles)
    {
        List<String> newCurrentRoles = roles.stream()
                .filter(role -> UserInfoHolder.getUserInfo().getAvailableRoles().contains(role))
                .collect(Collectors.toList());

        coreUtils.setUserSetting(UserInfoHolder.getUserName(), DatabaseConstants.CURRENT_ROLE_LIST,
                MetadataUtils.toInClause(roles));

        UserInfoHolder.getUserInfo().setCurrentRoles(newCurrentRoles);
    }

    protected List<String> parseRoles( String roles )
    {
        TreeSet<String> rolesList = new TreeSet<>();
        if( roles == null || "()".equals( roles ) )
        {
            return Collections.emptyList();
        }
        roles = roles.substring( 1, roles.length() - 1 ); // drop starting and trailing '(' ')'
        StringTokenizer st = new StringTokenizer( roles, "," );
        while( st.hasMoreTokens() )
        {
            rolesList.add( st.nextToken().trim().replaceAll( "'", "" ) );
        }
        return new ArrayList<>( rolesList );
    }

}
