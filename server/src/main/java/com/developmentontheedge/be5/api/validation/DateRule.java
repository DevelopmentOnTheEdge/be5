/** $Id: DateRule.java,v 1.4 2013/07/25 03:00:29 dimka Exp $ */

package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.model.Property;

import java.sql.Time;
import java.util.Date;

/**
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class DateRule extends AbstractRule
{
    public DateRule()
    {
        super( Validation.DATE, Validation.MESSAGE_DATE );
    }

    @Override
    public boolean isApplicable( Property property )
    {
        Class<?> clazz = getClassByOwner( property );

        if( Date.class.isAssignableFrom( clazz ) && !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) )
        {
            if( Time.class.isAssignableFrom( clazz ) )
            {
                setMessage( Validation.MESSAGE_TIME );
            }
            return true;
        }
        return false;
    }
}
