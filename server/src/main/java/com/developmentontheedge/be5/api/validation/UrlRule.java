/** $Id: UrlRule.java,v 1.4 2009/02/20 06:51:36 andrey Exp $ */
package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.model.Property;

/**
 * Requires the property to be a valid URL.
 *
 * @author Andrey Anisimov <andrey@developmentontheedge.com>
 */
public class UrlRule extends AbstractRule
{
    public UrlRule()
    {
        super( Validation.URL, Validation.MESSAGE_URL );
    }

    public boolean isApplicable( Property property )
    {
        String name = property.getName().toLowerCase();
        return !property.getBooleanAttribute( BeanInfoConstants.MULTIPLE_SELECTION_LIST ) && 
                ( name.endsWith( "url" ) && String.class.equals( property.getValueClass() ) );
    }
}
