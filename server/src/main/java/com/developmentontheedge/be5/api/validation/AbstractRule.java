/** $Id: AbstractRule.java,v 1.4 2013/07/25 03:00:28 dimka Exp $ */

package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.model.Property;

import java.util.logging.Logger;

/**
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public abstract class AbstractRule
{
    protected static final Logger log = Logger.getLogger(AbstractRule.class.getName());

    private String rule;

    private String message;

    protected AbstractRule( String rule, String message )
    {
        this.rule = rule;
        this.message = message;
    }

    /**
     * @param rule The string representation of the rule.
     */
    public void setRule( String rule )
    {
        this.rule = rule;
    }

    /**
     * @return Sting representation of the rule.
     */
    public String getRule()
    {
        return rule;
    }

    /**
     * @param message The message that will be displayed if case of invalid property value.
     */
    public void setMessage( String message )
    {
        this.message = message;
    }

    /**
     * @return Message to display if validation is failed.
     */
    public String getMessage()
    {
        return message;
    }


    //public abstract boolean isApplicable( Property property );

    /**
     * Checks whether the rule is applicable to the specified property.
     *
     * @param dynamicProperty DynamicProperty to apply the rule
     * @return true if the rule can be applied to the given property, false otherwise.
     */
    public abstract boolean isApplicable( DynamicProperty dynamicProperty );
    
    public static Class<?> getClassByOwner( Property property )
    {
        Object owner = property.getOwner();
        DynamicPropertySet dps = ((com.developmentontheedge.beans.model.Property.PropWrapper)owner).getOwner();

        Class<?> clazz = dps.getType(property.getDescriptor().getName());
        
        return clazz;
    }
}
