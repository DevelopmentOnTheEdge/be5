package com.developmentontheedge.be5.model;

import java.util.Objects;

import com.developmentontheedge.be5.api.experimental.v1.DynamicPropertyAttributes;

/**
 * Features of a field to improve UX.
 */
public class FieldClarifyingFeatures
{
    
    public static Builder builder()
    {
        return new Builder();
    }
    
    public static class Builder
    {
        private String placeholder;
        private String tooltip;
        private String helpText;
        
        Builder() {}
        
        public Builder placeholder(String value)
        {
            Objects.requireNonNull(value);
            this.placeholder = value;
            return this;
        }
        
        public Builder tooltip(String value)
        {
            Objects.requireNonNull(value);
            this.tooltip = value;
            return this;
        }
        
        public Builder helpText(String value)
        {
            Objects.requireNonNull(value);
            this.helpText = value;
            return this;
        }
        
        public FieldClarifyingFeatures build()
        {
            return new FieldClarifyingFeatures(this);
        }
    }
    
    /**
     * @see DynamicPropertyAttributes#PLACEHOLDER
     */
    public final String placeholder;
    
    /**
     * @see DynamicPropertyAttributes#TOOLTIP
     */
    public final String tooltip;
    
    /**
     * @see DynamicPropertyAttributes#HELP_TEXT
     */
    public final String helpText;
    
    FieldClarifyingFeatures(Builder builder)
    {
        this.placeholder = builder.placeholder;
        this.tooltip = builder.tooltip;
        this.helpText = builder.helpText;
    }
    
}
