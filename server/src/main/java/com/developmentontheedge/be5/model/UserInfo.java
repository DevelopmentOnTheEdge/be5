package com.developmentontheedge.be5.model;

/** $Id: UserInfo.java,v 1.20 2014/02/13 06:24:45 lan Exp $ */

import com.developmentontheedge.be5.metadata.RoleType;
import one.util.streamex.StreamEx;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class UserInfo implements Serializable
{
    private String userName;
    private Date creationTime;
    private String createdInThread;

    private List<String> availableRoles;
    private List<String> currentRoles;

    public String getUserName()
    {
        return userName;
    }

    public UserInfo(){}

    public UserInfo(String userName, Date creationTime)
    {
        this.userName = userName;
        this.creationTime = creationTime;
    }

    public void setUserName(String userName )
    {
        this.userName = userName;
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
        return currentRoles.contains(role);
    }

    public boolean isAdmin()
    {
        return currentRoles.contains(RoleType.ROLE_ADMINISTRATOR)
                || currentRoles.contains(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    public boolean isGuest()
    {
        return getUserName() == null;
    }

    public List<String> getAvailableRoles()
    {
        return availableRoles;
    }

    public void setAvailableRoles(List<String> availableRoles)
    {
        this.availableRoles = availableRoles;
    }

    public List<String> getCurrentRoles()
    {
        return currentRoles;
    }

    public void setCurrentRoles(List<String> currentRoles)
    {
        this.currentRoles = currentRoles;
    }

    public void selectRoles(List<String> roles)
    {
        currentRoles = StreamEx.of(roles).filter(role -> availableRoles.contains(role)).toList();
    }

}
