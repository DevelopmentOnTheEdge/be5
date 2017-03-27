/** $Id: RequestPreprocessor.java,v 1.2 2009/09/22 11:38:59 zha Exp $ */

package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.helpers.UserInfo;
import com.developmentontheedge.be5.api.services.DatabaseService;

import javax.servlet.http.HttpServletRequest;

abstract public class RequestPreprocessor
{
    protected DatabaseService databaseService;
    protected UserInfo userInfo;

    public RequestPreprocessor(DatabaseService databaseService, UserInfo userInfo )
    {
        this.databaseService = databaseService;
        this.userInfo = userInfo;
    }

    abstract public String preprocessUrl(HttpServletRequest request, String url );
}
