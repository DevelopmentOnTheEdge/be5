package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.helpers.impl.UserInfoManagerImpl;
//import com.developmentontheedge.enterprise.UserInfo;
import com.google.common.annotations.Beta;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

@Beta
public interface UserInfoManager {
    
    static UserInfoManager get(Request req, ServiceProvider serviceProvider)
    {
        return UserInfoManagerImpl.create(req, serviceProvider, serviceProvider.getMeta());
    }
    
    /**
     * Returns a preferred by the user language in lower case.
     */
    String getLanguage();
    
    /**
     * Return a preferred locale.
     */
    Locale getLocale();
    
    /**
     * Changes the preferred by the user language. The case of the language code is ignored.
     * 
     * @param language
     *            An ISO 639 alpha-2 or alpha-3 language code, or a language subtag up to 8 characters in length.
     *            See the Locale class description about valid language values.
     * @see Locale
     */
    void changeLanguage(String language);
    
    /**
     * Returns a current role set of the user.
     */
    List<String> getCurrentRoles();
    
    /**
     * Returns all the roles that the user can use.
     */
    List<String> getAvailableRoles();
    
    /**
     * Tries to change the user current roles.
     */
    void selectRoles(List<String> roles) throws Exception;
    
    /**
     * Tries to log in. Returns if it was succeed.
     * 
     * @see HttpServletResponse
     * @see HttpServletResponse#SC_OK
     * @see HttpServletResponse#SC_UNAUTHORIZED
     */
    boolean login(String username, String password);

    /**
     * Tries to log out.
     */
    void logout();

    /**
     * Returns a user name.
     */
    String getUserName();

    /**
     * Returns true is the user is not a guest.
     */
    boolean isLoggedIn();
    
    /**
     * Returns a session-related information. Note that this information is mutable.
     */
    @Deprecated
    UserInfo getUserInfo();
    
    /**
     * Changes the session information about the user: saves his name and network address.
     * @param userName usually the email of the user
     */
//    void setCurrentUser(String userName);
}
