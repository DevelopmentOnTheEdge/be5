package com.developmentontheedge.be5.legacy;

import javax.servlet.http.HttpSession;

import com.developmentontheedge.be5.UserInfo;
import com.developmentontheedge.be5.Utils;

/**
 * Doesn't allow to get session directly.
 * @see Utils.OperationUserInfo
 */
class SafeUserInfo extends UserInfo {
    
    private static final long serialVersionUID = 1L;
    
    public SafeUserInfo(UserInfo orig) {
        this.userName = orig.getUserName();
        this.curRoleList = orig.getCurRoleList();
        this.locale = orig.getLocale();
        this.remoteAddr = orig.getRemoteAddr();
        this.session = orig.getSession();
    }
    
    @Override
    public HttpSession getSession() {
        throw new SecurityException();
    }
    
    public com.beanexplorer.enterprise.Operation.SessionAdapter createSessionAdapter() {
        return Utils.createSessionAdapter(super.getSession());
    }
    
}
