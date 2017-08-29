/** $Id: PersistentRule.java,v 1.2 2009/03/20 12:42:10 zha Exp $ */

package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.model.Property;

/**
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class PersistentRule extends AbstractRule
{
    private String entityName;
    private String propertyName;

    /**
     * No-args constructor used to instantinate the rule via reflection.
     */
    public PersistentRule()
    {
        super( null, null );
    }

    public String getEntityName()
    {
        return entityName;
    }

    public void setEntityName( String entityName )
    {
        this.entityName = entityName;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public void setPropertyName( String propertyName )
    {
        this.propertyName = propertyName;
    }

    public boolean isApplicable( Property property )
    {
        // Persistent rules are always applicable
        return true;
    }
}
