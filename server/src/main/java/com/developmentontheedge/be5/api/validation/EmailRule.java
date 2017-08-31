/** $Id: EmailRule.java,v 1.7 2013/07/15 12:38:28 zha Exp $ */
package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.model.Property;

/**
 * Requires the property to be a valid email address.
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class EmailRule extends AbstractRule
{
    public EmailRule()
    {
        super( Validation.EMAIL, Validation.MESSAGE_EMAIL );
    }

//    public boolean isApplicable( Property property )
//    {
//        String name = property.getName().toLowerCase();
//        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && (
//           name.endsWith( "email" ) || name.equals( "emailaddress" ) ) && String.class.equals( property.getValueClass() );
//    }

    public boolean isApplicable( DynamicProperty property )
    {
        String name = property.getName().toLowerCase();
        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && (
                name.endsWith( "email" ) || name.equals( "emailaddress" ) ) && String.class.equals( property.getType() );
    }
}
