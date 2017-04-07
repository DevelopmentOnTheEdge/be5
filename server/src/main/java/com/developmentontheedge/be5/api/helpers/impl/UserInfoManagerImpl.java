package com.developmentontheedge.be5.api.helpers.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfo;
import com.developmentontheedge.be5.api.helpers.UserInfoManager;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.metadata.Utils;
import one.util.streamex.StreamEx;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.api.helpers.UserInfo.GUEST;
import static com.developmentontheedge.be5.metadata.RoleType.ROLE_ADMINISTRATOR;


public class UserInfoManagerImpl implements UserInfoManager {

    private static final Logger log = Logger.getLogger(UserInfoManagerImpl.class.getName());
    
    public static UserInfoManagerImpl create(Request req, ServiceProvider serviceProvider, Meta meta)
    {
        return new UserInfoManagerImpl(req, serviceProvider, meta);
    }
    
    private final Request req;
    private final HttpServletRequest rawRequest;
    private final HttpSession rawSession;
    /**
     * Cache. Note that this cache invalidation is useful only for components
     * that do some actions after calling {@link UserInfoManager#login(String, String)} or {@link UserInfoManager#logout()}.
     */
    private UserInfo user = null;
    private final ServiceProvider serviceProvider;
    private final SqlService db;
    private final DatabaseService databaseService;
    private final LoginService loginService;

    public UserInfoManagerImpl(Request req, ServiceProvider serviceProvider, Meta meta) {
        this.req = req;
        this.serviceProvider = serviceProvider;
        this.loginService = serviceProvider.get(LoginService.class);
        this.databaseService = serviceProvider.getDatabaseService();
        this.db = serviceProvider.getSqlService();
        this.rawRequest = req.getRawRequest();
        this.rawSession = req.getRawRequest().getSession();
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
            String selectRolesSql = "SELECT role_name FROM user_roles WHERE user_name = " + Utils.safestr(databaseService, getUserInfo().getUserName(), true);
            List<String> selectRoles = db.selectList(selectRolesSql, rs -> rs.getString(1));

            return Collections.unmodifiableList(selectRoles);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    public static String rolesToString( List<String> roles )
    {
        return Utils.toInClause( roles );
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

            List<String> languages = StreamEx.of(serviceProvider.getProject().getLanguages()).toList();

            if(!languages.contains(user.getLocale().getLanguage())){
                user.setLocale(new Locale( languages.get(0) ));
            }
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
        //return runLegacyLoginOperation(getLegacyLoginOperation(), username, password);

        if( HttpServletResponse.SC_OK == loginService.login(username, password, null, null)){
            return username;
        }
        return null;
    }

//    private String runLegacyLoginOperation(LegacyOperation legacyOperation, String username, String password) throws SQLException
//    {
//        //TODO
//        Login loginOperation = (Login) legacyOperation.getRawOperation();
//        loginOperation.setKey(null);
//        String sessionId = req.getSessionId();
//        String remoteAddr = req.getRemoteAddr();
//        int result = loginOperation.login(connector, remoteAddr, username, password, null, sessionId);
//
//        if (result == HttpServletResponse.SC_OK)
//        	return loginOperation.getActualUser();
//
//        return null;
//    }

//    private LegacyOperation getLegacyLoginOperation() throws GeneralSecurityException
//    {
//        UserInfo user = getUserInfo();
//
//        // See WebAppInitializer.
//        //TODO CryptoUtils.setPasswordAndAlgorithm("myHomeKey", "PBEWithMD5AndDES");
//
////        LegacyOperationFactory factory = serviceProvider.get(LegacyOperationsService.class).createFactory(user, getRequest());
////        LegacyOperation legacyOperation = factory.create(meta.getOperation("users", "Login", getCurrentRoles()), req, null, Collections.<String>emptyList());
//
////        return legacyOperation;
//        return null;
//    }
    
    private void saveCurrentUser(String username) {
        HttpSession session = getRequest().getSession();
        session.setAttribute("remoteAddr", req.getRemoteAddr());
        session.setAttribute(SessionConstants.CURRENT_USER, username);

        UserInfo ui = new UserInfo(username, new Date());
        ui.setLocale(req.getRawRequest().getLocale());
        //todo add roleService
        ui.setCurRoleList(rolesToString(getAvailableRoles()));

        session.setAttribute( SessionConstants.USER_INFO, ui );

        user = null; // invalidate
        // re-read user info in order to have it stored in the session and to avoid race conditions
        UserInfo newUserInfo = getUserInfo();
        log.info("Login as user: " + newUserInfo.getUserName());
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
            UserInfo userInfo = synchronizedGetUserInfo(request, request.getSession());
//            if(userInfo.getTimeZone() == null){
//                userInfo.setTimeZone(Utils.getUserSetting( connector, userInfo.getUserName(), "time-zone" ));
//                if(userInfo.getTimeZone() == null){
//                    userInfo.setTimeZone("Asia/Novosibirsk");
//                }
//            }
            return userInfo;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e, "Can't load an user info");
        }
    }

    private synchronized UserInfo synchronizedGetUserInfo(HttpServletRequest request, HttpSession session)
            throws Exception
    {
        UserInfo ui = (UserInfo) session.getAttribute(SessionConstants.USER_INFO);

        if(ui == null)return GUEST;

        return ui;
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

//    @Override
//    public void setCurrentUser(String userName)
//    {
//        rawSession.setAttribute("remoteAddr", rawRequest.getRemoteAddr());
//        rawSession.setAttribute(SessionConstants.CURRENT_USER, userName);
//        rawSession.removeAttribute(SessionConstants.USER_INFO);
//    }
    
}
