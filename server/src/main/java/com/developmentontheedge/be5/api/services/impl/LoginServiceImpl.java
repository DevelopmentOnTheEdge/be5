package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Session;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.impl.SessionImpl;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.serialization.ModuleLoader2;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.test.TestSession;
import one.util.streamex.StreamEx;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    private SqlService db;
    private ProjectProvider project;

    public LoginServiceImpl(SqlService db, ProjectProvider project)
    {
        this.db = db;
        this.project = project;
    }

    private boolean login(String user, String password)
    {
        try
        {
            String sql = "SELECT COUNT(user_name) FROM users WHERE user_name = ?";
            String passwordCheckClause = getPasswordCheckClause();
            sql += " AND ("+passwordCheckClause+")";

            if(db.getLong(sql, user, password) == 1L){
                return true;
            }
        }
        catch (SQLException e)
        {
            throw Be5Exception.internal(log, e);
        }
        catch( java.security.GeneralSecurityException gse )
        {
            log.log(Level.SEVERE,  "Encryption problem", gse);
        }
        catch( java.io.UnsupportedEncodingException uee )
        {
            log.log(Level.SEVERE, "Unexpected problem", uee );
        }
        return false;
    }

    private static String getPasswordCheckClause() throws SQLException,
            GeneralSecurityException, UnsupportedEncodingException
    {
//        if( passwordKey != null )
//        {
//            String encFunc = connector.getAnalyzer().makeEncryptExpr( password, passwordKey );
//            password = ( encFunc != null ) ? encFunc : Utils.safestr( connector, password, true );
//            return "user_pass = " + password;
//        }

//        if( Utils.columnExists( connector, "users", DatabaseConstants.ENCRYPT_COLUMN_PREFIX + "user_pass" ) )
//        {
//            return encName + " = '" + CryptoUtils.encrypt( password ) + "'" + " OR user_pass = "
//                    + Utils.safestr( connector, password, true );
//        }

        return "user_pass = ?";
    }

    private List<String> selectAvailableRoles(String username)
    {
        return db.selectList("SELECT role_name FROM user_roles WHERE user_name = ?",
                    rs -> rs.getString(1), username);
    }

    public boolean login(Request req, String username, String password)
    {
        try
        {
            if (login(username, password))
            {
                saveUser(username, req);
                return true;
            }

            return false;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    @Override
    public void saveUser(String username, Request req)
    {
        List<String> availableRoles = selectAvailableRoles(username);
        if(ModuleLoader2.getDevRoles().size() > 0)
        {
            availableRoles.addAll(ModuleLoader2.getDevRoles());
        }

        UserInfo ui = saveUser(username, availableRoles, req.getRawRequest().getLocale(), req.getRemoteAddr(), req.getSession());

        HttpSession session = req.getRawSession();
        session.setAttribute("remoteAddr", req.getRemoteAddr());
        session.setAttribute(SessionConstants.USER_INFO, ui);
        session.setAttribute(SessionConstants.CURRENT_USER, ui.getUserName());

        log.fine("Login user: " + username);
    }

    @Override
    public UserInfo saveUser(String userName, List<String> availableRoles, Locale locale, String remoteAddr, Session session)
    {
        UserInfo ui = new UserInfo(userName, availableRoles, session);
        ui.setRemoteAddr(remoteAddr);

        UserInfoHolder.setUserInfo(ui);
        setLanguage(locale);
        return ui;
    }

    @Override
    public void setLanguage(Locale locale)
    {
        List<String> languages = StreamEx.of(project.getProject().getLanguages()).toList();

        if(languages.contains(locale.getLanguage()))
        {
            UserInfoHolder.getUserInfo().setLocale(locale);
        }
        else
        {
            UserInfoHolder.getUserInfo().setLocale(new Locale( languages.get(0) ));
        }
    }

    @Override
    public void logout(Request req)
    {
        HttpSession session = req.getRawSession();
        session.removeAttribute( SessionConstants.USER_INFO );
        session.invalidate();

        String username = UserInfoHolder.getUserName();
        UserInfoHolder.setUserInfo(null);
        log.info("Logout user: " + username);
    }

    @Override
    public void initGuest(Request req)
    {
        Locale locale = Locale.US;
        String remoteAddr = "";

        Session session;
        if(req != null)
        {
            locale = req.getRawRequest().getLocale();
            remoteAddr = req.getRemoteAddr();
            session = req.getSession();
        }
        else
        {
            session = new TestSession();
        }

        if(ModuleLoader2.getDevRoles().size() > 0)
        {
            saveUser("dev", ModuleLoader2.getDevRoles(), locale, remoteAddr, session);
        }
        else
        {
            saveUser("Guest", Collections.singletonList(RoleType.ROLE_GUEST), locale, remoteAddr, session);
        }
    }

}
