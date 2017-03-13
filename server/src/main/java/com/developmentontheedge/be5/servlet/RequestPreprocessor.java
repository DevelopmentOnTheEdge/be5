/** $Id: RequestPreprocessor.java,v 1.2 2009/09/22 11:38:59 zha Exp $ */

package com.developmentontheedge.be5.servlet;

import com.developmentontheedge.be5.api.helpers.UserInfo;
import com.developmentontheedge.dbms.DbmsConnector;

import javax.servlet.http.HttpServletRequest;

abstract public class RequestPreprocessor
{
    protected DbmsConnector connector;
    protected UserInfo userInfo;

    public RequestPreprocessor(DbmsConnector connector, UserInfo userInfo )
    {
        this.connector = connector;
        this.userInfo = userInfo;
    }

    abstract public String preprocessUrl(HttpServletRequest request, String url );
}
