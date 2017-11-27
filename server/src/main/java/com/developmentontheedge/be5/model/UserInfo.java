package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.api.Session;
import one.util.streamex.StreamEx;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class UserInfo implements Serializable
{
    private String userName;
    private Date creationTime;

    private List<String> availableRoles;
    private List<String> currentRoles;

    public String getUserName()
    {
        return userName;
    }

    public UserInfo(String userName, List<String> availableRoles, Session session)
    {
        this.userName = userName;
        this.session = session;
        this.availableRoles = new ArrayList<>(availableRoles);
        this.currentRoles = new ArrayList<>(availableRoles);

        this.creationTime = new Date();
        this.locale = Locale.US;
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

    Session session;
    public Session getSession()
    {
        return session;
    }

    public boolean isSessionSafe()
    {
        return false;
    }


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

    public void setRemoteAddr(String remoteAddr)
    {
        this.remoteAddr = remoteAddr;
    }

    public Date getCreationTime()
    {
        return creationTime;
    }

    public boolean isUserInRole( String role )
    {
        return currentRoles.contains(role);
    }

    public List<String> getAvailableRoles()
    {
        return availableRoles;
    }

    public List<String> getCurrentRoles()
    {
        return currentRoles;
    }

    public void setRoles(List<String> roles)
    {
        this.availableRoles = roles;
        this.currentRoles = roles;
    }

    public void selectRoles(List<String> roles)
    {
        currentRoles = StreamEx.of(roles).filter(role -> availableRoles.contains(role)).toList();
    }

    @Override
    public String toString()
    {
        return "UserInfo{" +
                "userName='" + userName + '\'' +
                ", locale=" + locale +
                ", timeZone=" + timeZone +
                ", remoteAddr='" + remoteAddr + '\'' +
                '}';
    }

    public String getLanguage()
    {
        return getLocale().getLanguage();
    }
}
