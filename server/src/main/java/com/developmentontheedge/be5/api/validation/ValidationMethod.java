/** $Id: ValidationMethod.java,v 1.4 2014/04/12 09:35:36 zha Exp $ */

package com.developmentontheedge.be5.api.validation;

import java.io.Serializable;

public class ValidationMethod implements Serializable
{
    private String rule;
    private String code;
    private String defaultMessage;

    private boolean bMissing;

    public boolean isMissing()
    {
        return bMissing;
    }

    public void setMissing( boolean bMissing )
    {
        this.bMissing = bMissing;
    }

    public String getRule()
    {
        return rule;
    }

    public void setRule( String rule )
    {
        this.rule = rule;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode( String code )
    {
        this.code = code;
    }

    public String getDefaultMessage()
    {
        return defaultMessage;
    }

    public void setDefaultMessage( String defaultMessage )
    {
        this.defaultMessage = defaultMessage;
    }
}
