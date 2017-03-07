package com.developmentontheedge.be5.api.helpers.impl;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import com.developmentontheedge.be5.legacy.UserInfo;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.api.services.Meta;

import com.developmentontheedge.be5.legacy.LegacyOperation;
import com.developmentontheedge.be5.legacy.LegacyOperationFactory;
import com.developmentontheedge.be5.legacy.LegacyOperationsService;

import static com.developmentontheedge.be5.metadata.RoleType.ROLE_ADMINISTRATOR;


public class UserInfoManagerImpl implements UserInfoManager {
    
    public static UserInfoManagerImpl create(Request req, ServiceProvider serviceProvider, DatabaseConnector connector, Meta meta)
    {
        return new UserInfoManagerImpl(req, serviceProvider, connector, meta);
    }
    
    private final Request req;
    private final HttpServletRequest rawRequest;
    private final HttpSession rawSession;
    /**
     * Cache. Note that this cache invalidation is useful only for components
     * that do some actions after calling {@link UserInfoManager#login(String, String)} or {@link UserInfoManager#logout()}.
     */
    private UserInfo user = null;
    private final DatabaseConnector connector;
    private final Meta meta;
    private final ServiceProvider serviceProvider;
    
    public UserInfoManagerImpl(Request req, ServiceProvider serviceProvider, DatabaseConnector connector, Meta meta) {
        this.req = req;
        this.serviceProvider = serviceProvider;
        this.rawRequest = req.getRawRequest();
        this.rawSession = req.getRawRequest().getSession();
        this.connector = connector;
        this.meta = meta;
    }
    
    @Override
    public String getLanguage() {
        return getLocale().getLanguage().toLowerCase();
    }
    
    @Override
    public Locale getLocale() {
        return getUserInfo().getLocale();
    }
    
    @Override
    public void changeLanguage(String language) {
        getUserInfo().setLocale(new Locale(language));
    }
    
    @Override
    public List<String> getCurrentRoles() {
        return getUserInfo().getCurrentRoleList();
    }
    
    @Override
    public List<String> getAvailableRoles() {
        try
        {
        	if(getUserInfo().isGuest()) {
        		return Collections.singletonList("Guest");
        	}
            if(getUserInfo().isAdmin()) {
                return Collections.singletonList(ROLE_ADMINISTRATOR);
            }
//TODO            String selectRolesSql = "SELECT role_name FROM user_roles WHERE user_name = " + Utils.safestr(connector, getUserInfo().getUserName(), true);
//            String[] roles = (String[]) Utils.readAsArray(connector, selectRolesSql, new String[0]);

            //return Collections.unmodifiableList(Arrays.asList(roles));
            return Collections.singletonList("Guest");
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    @Override
    public void selectRoles(List<String> roles) throws Exception {
        //TODO RoleUtils.assignRoles(connector, getUserInfo(), roles);
    }
    
    private HttpServletRequest getRequest() {
        return rawRequest;
    }
    
    @Override
    public UserInfo getUserInfo() {
        if (user == null)
        {
            user = getUserInfo(rawRequest);
        }
        
        return user;
    }

    @Override
    public boolean login(String username, String password) {
        try
        {
            String actualUser = verify(username, password);

            if (actualUser != null)
            {
                saveCurrentUser(actualUser);
            }

            return actualUser != null;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }
    
    /**
     * Tests if the login is correct and returns an username if the login is correct, otherwise returns null.
     */
    private String verify(String username, String password) throws Exception {
        return runLegacyLoginOperation(getLegacyLoginOperation(), username, password);
    }

    private String runLegacyLoginOperation(LegacyOperation legacyOperation, String username, String password) throws SQLException
    {
        //TODO
//        Login loginOperation = (Login) legacyOperation.getRawOperation();
//        loginOperation.setKey(null);
//        String sessionId = req.getSessionId();
//        String remoteAddr = req.getRemoteAddr();
//        int result = loginOperation.login(connector, remoteAddr, username, password, null, sessionId);
//
//        if (result == HttpServletResponse.SC_OK)
//        	return loginOperation.getActualUser();
        
        return null;
    }

    private LegacyOperation getLegacyLoginOperation() throws GeneralSecurityException
    {
        UserInfo user = getUserInfo();
        
        // See WebAppInitializer.
        //TODO CryptoUtils.setPasswordAndAlgorithm("myHomeKey", "PBEWithMD5AndDES");
        
        LegacyOperationFactory factory = serviceProvider.get(LegacyOperationsService.class).createFactory(user, getRequest());
        LegacyOperation legacyOperation = factory.create(meta.getOperation("users", "Login", getCurrentRoles()), req, null, Collections.<String>emptyList());
        
        return legacyOperation;
    }
    
    private void saveCurrentUser(String username) {
        HttpSession session = getRequest().getSession();
        session.setAttribute("remoteAddr", req.getRemoteAddr());
        session.setAttribute(SessionConstants.CURRENT_USER, username);
        session.removeAttribute(SessionConstants.USER_INFO);

        user = null; // invalidate
        // re-read user info in order to have it stored in the session and to avoid race conditions
        UserInfo newUserInfo = getUserInfo();
        System.out.println("Hi, " + newUserInfo.getUserName() + "!");
    }

    @Override
    public void logout() {
        HttpSession session = getRequest().getSession();
        session.removeAttribute( SessionConstants.USER_INFO );
        session.invalidate();
        
        user = null; // invalidate
    }
    
    private UserInfo getUserInfo(HttpServletRequest request) {
        try
        {
            //TODO return Utils.getUserInfo(connector, request);
            return null;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e, "Can't load an user info");
        }
    }

    @Override
    public String getUserName()
    {
        return getUserInfo().getUserName();
    }
    
    public boolean isLoggedIn()
    {
        return getUserInfo().getUserName() != null;
    }

    @Override
    public void setCurrentUser(String userName)
    {
        rawSession.setAttribute("remoteAddr", rawRequest.getRemoteAddr());
        rawSession.setAttribute(SessionConstants.CURRENT_USER, userName);
        rawSession.removeAttribute(SessionConstants.USER_INFO);
    }
    
}
