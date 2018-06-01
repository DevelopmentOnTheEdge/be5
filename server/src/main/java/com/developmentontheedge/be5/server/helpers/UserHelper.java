package com.developmentontheedge.be5.server.helpers;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.server.servlet.UserInfoHolder;
import com.developmentontheedge.be5.web.Request;
import com.developmentontheedge.be5.web.Session;
import com.developmentontheedge.be5.web.SessionConstants;
import com.google.inject.Stage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;


public class UserHelper
{
    public final Logger log = Logger.getLogger(UserHelper.class.getName());

    private final Meta meta;
    private final Stage stage;

    @Inject
    public UserHelper(Meta meta, Stage stage)
    {
        this.meta = meta;
        this.stage = stage;
    }

    public UserInfo saveUser(String userName, List<String> availableRoles, List<String> currentRoles,
                             Locale locale, String remoteAddr, Session session)
    {
        if(stage != Stage.PRODUCTION && ModuleLoader2.getDevRoles().size() > 0)
        {
            Set<String> hs = new HashSet<>();
            hs.addAll(availableRoles);
            hs.addAll(ModuleLoader2.getDevRoles());

            availableRoles = new ArrayList<>(hs);
            currentRoles = new ArrayList<>(hs);

            log.info("Dev roles added - " + ModuleLoader2.getDevRoles().toString());
        }

        UserInfo ui = new UserInfo(userName, availableRoles, currentRoles);
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

        initGuest(req);

        log.info("Logout user: " + username);
    }

    public void initGuest(Request req)
    {
        Objects.requireNonNull(req);

        List<String> roles = Collections.singletonList(RoleType.ROLE_GUEST);

        saveUser(RoleType.ROLE_GUEST, roles, roles, req.getLocale(), req.getRemoteAddr(), req.getSession());
    }

}