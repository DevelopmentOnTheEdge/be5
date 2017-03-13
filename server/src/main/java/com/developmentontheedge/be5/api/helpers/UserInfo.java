package com.developmentontheedge.be5.api.helpers;

/** $Id: UserInfo.java,v 1.20 2014/02/13 06:24:45 lan Exp $ */

import com.developmentontheedge.be5.metadata.RoleType;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.StringTokenizer;

public class UserInfo implements Serializable
{
    protected String userName;
    protected Date creationTime;
    protected String createdInThread;

    public String getUserName()
    {
        return userName;
    }

    public void setUserName( String userName )
    {
        this.userName = userName;
    }

    protected String curRoleList;

    public String getCurRoleList()
    {
        return curRoleList;
    }

    public void setCurRoleList( String curRoleList )
    {
        this.curRoleList = curRoleList;
    }

    public List<String> getCurrentRoleList()
    {
        List<String> list = new ArrayList<>();

        if( getCurRoleList() != null )
        {
            StringTokenizer roles = new StringTokenizer( getCurRoleList(), "()'," );

            while( roles.hasMoreTokens() )
            {
                list.add( roles.nextToken() );
            }
        }

        return list;
    }

    protected Locale locale;
    public Locale getLocale()
    {
        String lang = locale.getLanguage();
        if( !"kz".equals( lang ) )
            return locale;
        // fix for incorrect Kyrgyz language code in IE 6.0
        // should be 'ky' but IE sets it to 'kz'
        return new Locale( "ky", locale.getCountry(), locale.getVariant() );
    }

    public void setLocale( Locale locale )
    {
        this.locale = locale;
    }

    protected TimeZone timeZone;
    public TimeZone getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone( TimeZone timeZone )
    {
        this.timeZone = timeZone;
    }

    public void setTimeZone( String timeZoneID )
    {
        this.timeZone = timeZoneID != null ? TimeZone.getTimeZone( timeZoneID ) : null;
    }

    transient protected HttpSession session;
    public HttpSession getSession()
    {
        return session;
    }

    public boolean isSessionSafe()
    {
        return false;
    }

//    public Operation.SessionAdapter createSessionAdapter()
//    {
//        return Utils.createSessionAdapter( getSession() );
//    }

    protected Timestamp loggedInTime;
    public Timestamp getLoggedInTime()
    {
        return loggedInTime;
    }

    protected Timestamp prevLoggedInTime;
    public Timestamp getPrevLoggedInTime()
    {
        return prevLoggedInTime;
    }

    protected String remoteAddr;
    public String getRemoteAddr()
    {
        return remoteAddr;
    }

    public String getCreatedInThread()
    {
        return createdInThread;
    }

    public Date getCreationTime()
    {
        return creationTime;
    }

    public boolean isUserInRole( String role )
    {
        role = role.trim();
        return getCurRoleList() != null && getCurRoleList().contains("'" + role + "'");
    }

    public boolean isAdmin()
    {
        return getCurRoleList() != null &&
                (
                        getCurRoleList().contains("'" + RoleType.ROLE_ADMINISTRATOR + "'") ||
                                getCurRoleList().contains("'" + RoleType.ROLE_SYSTEM_DEVELOPER + "'")
                );
    }

    public boolean isGuest()
    {
        return getUserName() == null;
    }

    public static final UserInfo ADMIN = new UserInfo()
    {
        public String getUserName()
        {
            return RoleType.ROLE_ADMINISTRATOR;
        }

        public String getCurRoleList()
        {
            return "('" + RoleType.ROLE_ADMINISTRATOR + "')";
        }

        public Locale getLocale()
        {
            try
            {
                //return new Locale( Utils.getSystemSetting( Utils.getDefaultConnector(), "FORCED_LOCALE", "en_US" ) );
                return Locale.US;
            }
            catch( Exception exc )
            {
                return Locale.US;
            }
        }
    };

    public static final UserInfo ADMIN_NODB = new UserInfo()
    {
        public String getUserName()
        {
            return RoleType.ROLE_ADMINISTRATOR;
        }

        public String getCurRoleList()
        {
            return "('" + RoleType.ROLE_ADMINISTRATOR + "')";
        }

        public Locale getLocale()
        {
            return Locale.US;
        }
    };

    public static final UserInfo GUEST = new UserInfo()
    {
        public String getUserName()
        {
            return null;
        }

        public String getCurRoleList()
        {
            return "('" + RoleType.ROLE_GUEST + "')";
        }

        public Locale getLocale()
        {
            try
            {
                //return new Locale( Utils.getSystemSetting( Utils.getDefaultConnector(), "FORCED_LOCALE", "en_US" ) );
                return new Locale( "en_US" );
            }
            catch( Exception exc )
            {
                return Locale.US;
            }
        }
    };
}
