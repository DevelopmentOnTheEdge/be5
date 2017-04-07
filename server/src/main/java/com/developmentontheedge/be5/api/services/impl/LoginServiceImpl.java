package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.exceptions.impl.Be5ErrorCode;
import com.developmentontheedge.be5.api.services.DatabaseService;
import com.developmentontheedge.be5.api.services.LoginService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.Utils;
import com.developmentontheedge.dbms.DbmsConnector;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginServiceImpl implements LoginService
{
    public static final Logger log = Logger.getLogger(LoginServiceImpl.class.getName());

    private String userName;

    private DatabaseService databaseService;
    private SqlService db;


    public LoginServiceImpl(DatabaseService databaseService, SqlService db)
    {
        this.databaseService = databaseService;
        this.db = db;
    }


    public String getActualUser()
    {
        return userName;
    }

    public int login(String user,
                     String password, String passwordKey, String sessionId)
    {
        try
        {
            String sql = "SELECT COUNT(user_name) FROM users WHERE user_name = " + Utils.safestr( databaseService, user, true );
            String passwordCheckClause = getPasswordCheckClause( databaseService, password, passwordKey );
            sql += " AND ("+passwordCheckClause+")";

            //todo improve SqlService db.selectScalar(sql) != 1
            if(!db.selectScalar(sql).equals(Long.parseLong("1"))){
                return HttpServletResponse.SC_UNAUTHORIZED;
            }
        }
        catch (SQLException e)
        {
            throw Be5ErrorCode.INTERNAL_ERROR.rethrow(log, e);
        }
        catch( java.security.GeneralSecurityException gse )
        {
            log.log(Level.SEVERE,  "Encryption problem", gse);
            errorMessage = "Encryption problem: " + gse.getMessage();
            return HttpServletResponse.SC_UNAUTHORIZED;
        }
        catch( java.io.UnsupportedEncodingException uee )
        {
            log.log(Level.SEVERE, "Unexpected problem", uee );
            errorMessage = "Unexpected problem: " + uee.getMessage();
            return HttpServletResponse.SC_UNAUTHORIZED;
        }


        this.userName = user;
        return HttpServletResponse.SC_OK; // 200
    }

    public static String getPasswordCheckClause(DbmsConnector connector, String password, String passwordKey) throws SQLException,
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

    private String errorMessage = null;

    /** Returns message that will be shown when authorisation is failed. */
    public String getErrorMessage()
    {
        return errorMessage;
    }
}
