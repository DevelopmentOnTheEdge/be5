package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.model.UserInfo;
import com.developmentontheedge.be5.api.helpers.UserInfoHolder;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.SessionConstants;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.dbms.DbmsConnector;
import one.util.streamex.StreamEx;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.RoleType.ROLE_ADMINISTRATOR;

public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    private DatabaseService databaseService;
    private SqlService db;
    private ProjectProvider project;


    public LoginServiceImpl(DatabaseService databaseService, SqlService db, ProjectProvider project)
    {
        this.databaseService = databaseService;
        this.db = db;
        this.project = project;
    }

    public boolean login(String user, String password)
    {
        try
        {
            String sql = "SELECT COUNT(user_name) FROM users WHERE user_name = " + Utils.safestr( databaseService, user, true );
            String passwordCheckClause = getPasswordCheckClause( databaseService, password );
            sql += " AND ("+passwordCheckClause+")";

            //todo improve SqlService db.selectScalar(sql) == 1
            if(db.selectScalar(sql).equals(Long.parseLong("1"))){
                return true;
            }
        }
        catch (SQLException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
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

    public static String getPasswordCheckClause(DbmsConnector connector, String password) throws SQLException,
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

        return "user_pass = " + Utils.safestr( connector, password, true );
    }

    private List<String> getAvailableRoles(String username) {
        try
        {
            String selectRolesSql = "SELECT role_name FROM user_roles WHERE user_name = " + Utils.safestr(databaseService, username, true);
            List<String> selectRoles = db.selectList(selectRolesSql, rs -> rs.getString(1));

            return Collections.unmodifiableList(selectRoles);
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    public boolean login(Request req, String username, String password) {
        try
        {
            if (login(username, password))
            {
                saveCurrentUser(req, username);
                return true;
            }

            return false;
        }
        catch (Exception e)
        {
            throw Be5Exception.internal(e);
        }
    }

    private void saveCurrentUser(Request req, String username) {
        HttpSession session = req.getRawSession();
        session.setAttribute("remoteAddr", req.getRemoteAddr());
        session.setAttribute(SessionConstants.CURRENT_USER, username);

        UserInfo ui = new UserInfo(username, new Date());

        String roles = Utils.toInClause(getAvailableRoles(username));

        ui.setCurRoleList(roles);
        ui.setAvailableRoles(roles);

        ui.setLocale(req.getRawRequest().getLocale());

        List<String> languages = StreamEx.of(project.getProject().getLanguages()).toList();

        if(!languages.contains(ui.getLocale().getLanguage())){
            ui.setLocale(new Locale( languages.get(0) ));
        }

        session.setAttribute( SessionConstants.USER_INFO, ui );
        UserInfoHolder.setUserInfo(ui);

        log.info("Login as user: " + username);
    }

    @Override
    public void logout(Request req) {
        HttpSession session = req.getRawSession();
        session.removeAttribute( SessionConstants.USER_INFO );
        session.invalidate();

        UserInfoHolder.setUserInfo(null);
    }

}
