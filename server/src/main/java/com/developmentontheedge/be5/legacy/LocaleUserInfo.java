package com.developmentontheedge.be5.legacy;

import com.developmentontheedge.be5.api.helpers.UserInfo;
import com.developmentontheedge.be5.metadata.Utils;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Locale;
//import com.developmentontheedge.be5.Operation.SessionAdapter;

/**
 * UserInfo implementation which supports getLocale() call only
 * @author lan
 * TODO: change Legacy BE methods which accept UserInfo just to read Locale to accept Locale instead
 * @see Utils#readDictionaryLocalizations(String, com.beanexplorer.enterprise.DbmsConnector, UserInfo)
 * @see Utils#readQueryMessages(com.beanexplorer.enterprise.QueryInfo, com.beanexplorer.enterprise.DbmsConnector, UserInfo)
 * 
 */
public class LocaleUserInfo extends UserInfo
{
    private static final long serialVersionUID = 1L;

    public LocaleUserInfo(Locale locale)
	{
		this.locale = locale;
	}

	@Override
	public String getUserName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setUserName(String userName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCurRoleList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCurRoleList(String curRoleList) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<?> getCurrentRoleList() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLocale(Locale locale) {
		throw new UnsupportedOperationException();
	}

	@Override
	public HttpSession getSession() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSessionSafe() {
		throw new UnsupportedOperationException();
	}

//	@Override
//	public SessionAdapter createSessionAdapter() {
//		throw new UnsupportedOperationException();
//	}

	@Override
	public Timestamp getLoggedInTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Timestamp getPrevLoggedInTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRemoteAddr() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getCreatedInThread() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Date getCreationTime() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isUserInRole(String role) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAdmin() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isGuest() {
		throw new UnsupportedOperationException();
	}
}
