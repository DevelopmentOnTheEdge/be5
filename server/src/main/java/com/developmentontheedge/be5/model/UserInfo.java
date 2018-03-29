package com.developmentontheedge.be5.model;

import com.developmentontheedge.be5.api.Session;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;


public class UserInfo implements Serializable
{
    private String userName;
    private List<String> availableRoles;
    private List<String> currentRoles;

    private Session session;

    private Date creationTime;
    private Locale locale;
    private TimeZone timeZone;
    private String remoteAddr;
    private Timestamp prevLoggedInTime;
    private Timestamp loggedInTime;

    public UserInfo(String userName, List<String> availableRoles, List<String> currentRoles, Session session)
    {
        this.userName = userName;
        this.availableRoles = new ArrayList<>(availableRoles);
        this.currentRoles = new ArrayList<>(currentRoles);

        this.session = session;

        this.creationTime = new Date();
        this.locale = Locale.US;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName )
    {
        this.userName = userName;
    }

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

    public Session getSession()
    {
        return session;
    }

    public Timestamp getLoggedInTime()
    {
        return loggedInTime;
    }

    public Timestamp getPrevLoggedInTime()
    {
        return prevLoggedInTime;
    }

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

    public void setAvailableRoles(List<String> availableRoles)
    {
        this.availableRoles = availableRoles;
    }

    public void setCurrentRoles(List<String> currentRoles)
    {
        this.currentRoles = currentRoles;
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
