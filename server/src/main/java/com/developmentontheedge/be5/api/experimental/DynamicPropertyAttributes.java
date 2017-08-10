package com.developmentontheedge.be5.api.experimental;

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
    
}
