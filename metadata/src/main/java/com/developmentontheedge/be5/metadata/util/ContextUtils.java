package com.developmentontheedge.be5.metadata.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class ContextUtils
{
    public static String getContextParameter(String name)
    {
        try
        {
            InitialContext context = new InitialContext();
            Context xmlNode = (Context) context.lookup("java:comp/env");
            return (String) xmlNode.lookup(name);
        }
        catch (NamingException e)
        {
            return "NameNotFoundException";
        }
    }
}
