package com.developmentontheedge.be5.modules.core.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.modules.core.services.LoginService;
import com.developmentontheedge.be5.api.helpers.UserHelper;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;


public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    protected SqlService db;
    protected Meta meta;
    protected UserHelper userHelper;

    public LoginServiceImpl(SqlService db, Meta meta, UserHelper userHelper)
    {
        this.db = db;
        this.meta = meta;
        this.userHelper = userHelper;
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
        if(ModuleLoader2.getDevRoles().size() > 0)
        {
            availableRoles.addAll(ModuleLoader2.getDevRoles());
        }

        UserInfo ui = userHelper.saveUser(username, availableRoles, availableRoles,
                req.getLocale(), req.getRemoteAddr(), req.getSession());

        Session session = req.getSession();
        session.set("remoteAddr", req.getRemoteAddr());
        session.set(SessionConstants.USER_INFO, ui);
        session.set(SessionConstants.CURRENT_USER, ui.getUserName());

        log.fine("Login user: " + username);
    }

}
