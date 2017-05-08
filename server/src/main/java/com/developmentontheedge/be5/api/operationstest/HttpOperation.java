/** $Id: HttpOperation.java,v 1.13 2007/07/05 06:49:58 zha Exp $ */

package com.developmentontheedge.be5.api.operationstest;

import javax.servlet.http.Cookie;

public interface HttpOperation extends Be5Operation
{
    void setReferer(String referer);

    void setContextPrefix(String contextPrefix);

    void setIndexURL(String indexURL);
    void setQueryURL(String queryURL);
    void setCsvURL(String csvURL);
    void setOperationURL(String operationURL);

    String getCustomAction();

    String getCustomEnctype();

    String getCustomMethod();

    String getRedirectURL();

    //ExtraHttpAction[] getExtraActions();

    void setParamCookies(Cookie[] cookies);
    Cookie []getResultCookies();
}
