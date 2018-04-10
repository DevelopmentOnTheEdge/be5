package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.SessionConstants;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.test.TestSession;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;


public class UserHelper
{
    public final Logger log = Logger.getLogger(UserHelper.class.getName());

    private Meta meta;

    public UserHelper(Meta meta)
    {
        this.meta = meta;
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles,
                             Locale locale, String remoteAddr, Session session)
    {
        UserInfo ui = new UserInfo(userName, availableRoles, currentRoles, session);
        ui.setRemoteAddr(remoteAddr);
        ui.setLocale(meta.getLocale(locale));

        UserInfoHolder.setUserInfo(ui);

        session.set("remoteAddr", remoteAddr);
        session.set(SessionConstants.USER_INFO, ui);
        session.set(SessionConstants.CURRENT_USER, ui.getUserName());

        return ui;
    }

    public void logout(Request req)
    {
        req.getSession().invalidate();

        String username = UserInfoHolder.getUserName();
        UserInfoHolder.setUserInfo(null);

        log.info("Logout user: " + username);
    }

    public void initGuest(Request req)
    {
        Locale locale = Locale.US;
        String remoteAddr = "";

        Session session;
        if(req != null)
        {
            locale = req.getLocale();
            remoteAddr = req.getRemoteAddr();
            session = req.getSession();
        }
        else
        {
            session = new TestSession();
        }

        if(ModuleLoader2.getDevRoles().size() > 0)
        {
            saveUser("dev", ModuleLoader2.getDevRoles(), ModuleLoader2.getDevRoles(), locale, remoteAddr, session);
        }
        else
        {
            List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);
            saveUser(RoleType.ROLE_GUEST, roles, roles, locale, remoteAddr, session);
        }
    }

}
