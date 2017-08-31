/** $Id: RequiredRule.java,v 1.4 2009/02/24 08:17:17 andrey Exp $ */
package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.model.Property;
//import com.developmentontheedge.web.WebFormPropertyInspector;

/**
 * Requires the property to be filled (not null).
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class RequiredRule// extends AbstractRule
{
//    public RequiredRule()
//    {
//        super( Validation.REQUIRED, Validation.MESSAGE_REQUIRED );
//    }
//
//    public boolean isApplicable( Property property )
//    {
//        String[] tags = WebFormPropertyInspector.getTags( property );
//        if (!property.getBooleanAttribute(BeanInfoConstants.CAN_BE_NULL))
//            if (!Boolean.class.equals(property.getValueClass()))
//                if (!boolean.class.equals(property.getValueClass()))
//                    if ((tags == null) || !WebFormPropertyInspector.isBooleanTags(tags)) return true;
//        return false;
//    }
}
