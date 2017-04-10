package com.developmentontheedge.be5.api.experimental.v1;

import com.developmentontheedge.be5.api.operation.Option;
import com.developmentontheedge.be5.api.operation.Options;

/**
 * Attributes that aren't supported in BE3.
 * 
 * @author asko
 */
public class DynamicPropertyAttributes
{

    /**
     * Defines an HTML5 attribute "placeholder" for fields that support this attribute.
     * 
     * @see <a href="http://www.w3schools.com/tags/att_input_placeholder.asp">w3schools.com</a>
     */
    public static final String PLACEHOLDER = "--be5-placeholder";
    
    /**
     * Defines an HTML attribute "title" for a field.
     * 
     * @see <a href="http://www.w3schools.com/tags/att_global_title.asp">w3schools.com</a>
     */
    public static final String TOOLTIP = "--be5-tooltip";
    
    /**
     * Defines a block-level help text for an above input.
     * 
     * @see <a href="http://v4-alpha.getbootstrap.com/components/forms/#help-text">getbootstrap.com</a>
     */
    public static final String HELP_TEXT = "--be5-hint";
    
    /**
     * Defines a list of options in a "select" field. Can contain a list of
     * options or a enumeration class.
     * 
     * @see Option
     * @see Options
     */
    public static final String SELECT_OPTIONS = "--be5-select-options";
    
}
