/** $Id: NumberRule.java,v 1.5 2013/07/25 03:00:29 dimka Exp $ */
package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.model.Property;

/**
 * Requires the property to contain digits and maybe floating point.
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class NumberRule extends AbstractRule
{
    public NumberRule()
    {
        super( Validation.NUMBER, Validation.MESSAGE_NUMBER );
    }

    @Override
    public boolean isApplicable( Property property )
    {
        Class<?> clazz = getClassByOwner( property );

        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && ( 
               Number.class.isAssignableFrom( clazz ) ||
               short.class.isAssignableFrom( clazz ) ||
               int.class.isAssignableFrom( clazz ) ||
               long.class.isAssignableFrom( clazz ) ||
               float.class.isAssignableFrom( clazz ) ||
               double.class.isAssignableFrom( clazz ) );
    }
}
