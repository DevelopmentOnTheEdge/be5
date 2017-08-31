/** $Id: IntegerRule.java,v 1.3 2013/07/25 03:00:29 dimka Exp $ */
package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.model.Property;

import java.math.BigInteger;
import java.util.logging.Level;

/**
 * Requires the property to contain an integer number.
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class IntegerRule extends AbstractRule
{
    public IntegerRule()
    {
        super( Validation.INTEGER, Validation.MESSAGE_INTEGER );
    }

//    @Override
//    public boolean isApplicable( Property property )
//    {
//        Class<?> clazz = getClassByOwner( property );
//
//        if( clazz == null )
//        {
//            log.log(Level.SEVERE, "IntegerRule: Null value class for property " + property.getName() );
//        }
//
//        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && (
//                 Integer.class.isAssignableFrom( clazz ) ||
//                 int.class.isAssignableFrom( clazz ) ||
//                 Short.class.isAssignableFrom( clazz ) ||
//                 short.class.isAssignableFrom( clazz ) ||
//                 Long.class.isAssignableFrom( clazz ) ||
//                 long.class.isAssignableFrom( clazz ) ||
//                 BigInteger.class.isAssignableFrom( clazz ) );
//    }

    @Override
    public boolean isApplicable( DynamicProperty property )
    {
        Class<?> clazz = property.getType();

        if( clazz == null )
        {
            log.log(Level.SEVERE, "IntegerRule: Null value class for property " + property.getName() );
        }

        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && (
                Integer.class.isAssignableFrom( clazz ) ||
                        int.class.isAssignableFrom( clazz ) ||
                        Short.class.isAssignableFrom( clazz ) ||
                        short.class.isAssignableFrom( clazz ) ||
                        Long.class.isAssignableFrom( clazz ) ||
                        long.class.isAssignableFrom( clazz ) ||
                        BigInteger.class.isAssignableFrom( clazz ) );
    }
}
