/** $Id: SilentInsertOperation.java,v 1.19 2013/12/02 12:44:21 raptor Exp $ */

package com.developmentontheedge.be5.api.operationstest;

import com.developmentontheedge.be5.api.services.DatabaseService;

import javax.servlet.http.Cookie;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class SilentInsertOperation extends InsertOperation implements HttpOperation
{
    private Map myPars;

    public Object getParameters(Writer out, DatabaseService connector, Map presetValues ) throws Exception
    {
        myPars = presetValues;              
        return super.getParameters( out, connector, presetValues );
    }

    public void invoke( Writer out, DatabaseService connector ) throws Exception
    {
        super.invoke( new StringWriter(), connector );
    }

    protected String referer;
    public void setReferer( String referer )
    {
        this.referer = referer;
    }

    protected String contextPrefix;
    public void setContextPrefix( String contextPrefix )
    {
        this.contextPrefix = contextPrefix;
    }

    protected String indexURL;
    public void setIndexURL( String indexURL )
    {
        this.indexURL = indexURL;
    }

    protected String queryURL;
    public void setQueryURL( String queryURL )
    {
        this.queryURL = queryURL;
    }

    protected String csvURL;
    public void setCsvURL( String csvURL )
    {
        this.csvURL = csvURL;
    }

    protected String operationURL;
    public void setOperationURL( String operationURL )
    {
        this.operationURL = operationURL;
    }

    public String getCustomAction()
    {
        return null;
    }

    public String getCustomEnctype()
    {
        return null;
    }

    public String getCustomMethod()
    {
        return null;
    }

    public String getRedirectURL()
    {
        return "";//silentRedirectURL( queryURL, referer, myPars );
    }

//    public ExtraHttpAction[] getExtraActions()
//    {
//        return null;
//    }

    protected Cookie []cookies;
    public void setParamCookies( Cookie []cookies )
    {
        this.cookies = cookies;
    }

    public Cookie []getResultCookies()
    {
        return cookies;
    }
}
