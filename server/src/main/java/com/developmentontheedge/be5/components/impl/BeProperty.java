package com.developmentontheedge.be5.components.impl;

/**
 * Extracts well-known BeanExplorer3 property attributes.
 * 
 * @author asko
 */
public class BeProperty
{
//
//    private final Property property;
//
//    public BeProperty(Property property)
//    {
//        this.property = property;
//    }
//
//    public boolean isJavaClassProperty()
//    {
//        return WebFormPropertyInspector.isJavaClassProperty(property);
//    }
//
//    public String getName()
//    {
//        return property.getName();
//    }
//
//
//    public boolean isReadOnly()
//    {
//        return property.getBooleanAttribute(BeanInfoConstants.READ_ONLY);
//    }
//
//    public boolean canBeNull()
//    {
//        return property.getBooleanAttribute(BeanInfoConstants.CAN_BE_NULL)
//            || property.getBooleanAttribute(WebFormPropertyInspector.FORCE_CAN_BE_NULL);
//    }
//
//    public boolean reloadOnChange()
//    {
//        return property.getBooleanAttribute(BeanInfoConstants.RELOAD_ON_CHANGE);
//    }
//
//    public boolean isInGroup()
//    {
//        return getGroupId() != null;
//    }
//
//    public String getGroupName()
//    {
//        return property.getStringAttribute(BeanInfoConstants.GROUP_NAME);
//    }
//
//    public String getGroupId()
//    {
//        return property.getStringAttribute(BeanInfoConstants.GROUP_ID);
//    }
//
//    public boolean isDate()
//    {
//        return property.getValueClass() == java.sql.Date.class;
//    }
//
//    public boolean isDateTime()
//    {
//        FeatureDescriptor descriptor = property.getDescriptor();
//
//        if (descriptor instanceof PropertyDescriptor)
//        {
//            if (((PropertyDescriptor) descriptor).getPropertyType() == LocalDateTime.class)
//                return true;
//        }
//
//        Class<?> valueClass = property.getValueClass();
//
//        return valueClass == java.sql.Timestamp.class || valueClass == LocalDateTime.class;
//    }
//
//    public boolean isBool()
//    {
//        return WebFormPropertyInspector.isBooleanTags(getLegacyEnum()) || property.getValueClass() == Boolean.class;
//    }
//
//    public boolean isAutoComplete()
//    {
//        return getExternalEntityName() != null;
//    }
//
//    public boolean isEnum()
//    {
//        return getEnum() != null || getLegacyEnum() != null;
//    }
//
//    public boolean isMultilineText()
//    {
//        FeatureDescriptor descriptor = property.getDescriptor();
//        Object colSize = descriptor.getValue(WebFormPropertyInspector.COLUMN_SIZE_ATTR);
//        Object nCols = descriptor.getValue(WebFormPropertyInspector.NCOLUMNS_ATTR);
//        Object nRows = descriptor.getValue(WebFormPropertyInspector.NROWS_ATTR);
//
//        return colSize != null && Integer.parseInt(String.valueOf(colSize)) > 255 || nCols != null || nRows != null;
//    }
//
//    public boolean isPassword()
//    {
//        return property.getBooleanAttribute(BeanInfoConstants.PASSWORD_FIELD);
//    }
//
//    public List<Option> getEnumOptions()
//    {
//        List<Option> enam = getEnum();
//
//        if (enam != null)
//        {
//            return enam;
//        }
//
//        return toSelectOptions(getLegacyEnum());
//    }
//
//    public String getExternalEntityName()
//    {
//        Object tagList = property.getAttribute(BeanInfoConstants.EXTERNAL_TAG_LIST);
//        if(tagList instanceof String)
//            return (String)tagList;
//        return null;
//    }
//
//    public boolean autoRefresh()
//    {
//        return "this.form.submit()".equals(getOnChange());
//    }
//
//    public String getAsStr()
//    {
//        Object value = property.getValue();
//
//        if (value == null)
//        {
//            return "";
//        }
//
//        if(value instanceof Object[])
//        {
//            value = ((Object[])value)[0];
//        }
//
//        if (!(value instanceof String || value instanceof Number || value instanceof Date || value instanceof Boolean))
//        {
//            throw Be5Exception.internal("Illegal object supplied: " + value);
//        }
//
//        return value.toString();
//    }
//
//    public OptionalInt getNumberOfCulumns()
//    {
//        FeatureDescriptor descriptor = property.getDescriptor();
//        Object nColsObj = descriptor.getValue(WebFormPropertyInspector.NCOLUMNS_ATTR);
//
//        if (nColsObj == null)
//            return OptionalInt.empty();
//
//        return OptionalInt.of(Integer.parseInt(nColsObj.toString()));
//    }
//
//    public OptionalInt getNumberOfRows()
//    {
//        FeatureDescriptor descriptor = property.getDescriptor();
//        Object nRowsObj = descriptor.getValue(WebFormPropertyInspector.NROWS_ATTR);
//
//        if (nRowsObj == null)
//            return OptionalInt.empty();
//
//        return OptionalInt.of(Integer.parseInt(nRowsObj.toString()));
//    }
//
//    public Optional<String> getPlaceholder()
//    {
//        return Optional.ofNullable((String) property.getAttribute(DynamicPropertyAttributes.PLACEHOLDER));
//    }
//
//    public Optional<String> getHelpText()
//    {
//        return Optional.ofNullable((String) property.getAttribute(DynamicPropertyAttributes.HELP_TEXT));
//    }
//
//    public Optional<String> getTooltip()
//    {
//        return Optional.ofNullable((String) property.getAttribute(DynamicPropertyAttributes.TOOLTIP));
//    }
//
//    private List<Option> getEnum()
//    {
//        Object selectOptions = property.getAttribute(DynamicPropertyAttributes.SELECT_OPTIONS);
//
//        if (selectOptions instanceof Class)
//        {
//            Class<?> klass = (Class<?>) selectOptions;
//
//            if (klass.isEnum())
//            {
//                @SuppressWarnings("unchecked") // this cast is
//                Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) klass;
//
//                return Options.of(enumClass);
//            }
//
//            throw new AssertionError("Unsupported type " + klass.getName() + " as list of combo box items. It must be a enumeration.");
//        }
//
//        return (List<Option>) selectOptions;
//    }
//
//    /**
//     * Returns a list of pairs separated by a null character ('\u0000').
//     */
//    private String[] getLegacyEnum()
//    {
//        return WebFormPropertyInspector.getTags(property);
//    }
//
//    private List<Option> toSelectOptions(String[] magicEnumValues)
//    {
//        return StreamEx.of(magicEnumValues).map(this::magicValueToOption).toList();
//    }
//
//    private Option magicValueToOption(String magicValue)
//    {
//        String[] splitted = magicValue.split(OperationSupport.TAG_DELIMITER);
//
//        if (splitted.length < 2)
//        {
//            return new Option(magicValue, magicValue);
//        }
//
//        String optionText = splitted[0];;
//        String optionValue = splitted[1];
//
//        return new Option(optionValue, optionText);
//    }
//
//    private Object getOnChange()
//    {
//        return getExtraAttributes().get("onChange");
//    }
//
//    private Map<String, Object> getExtraAttributes()
//    {
//        Object attribute = property.getAttribute(BeanInfoConstants.EXTRA_ATTRS);
//
//        if (attribute instanceof Map)
//            return (Map<String, Object>) attribute;
//
//        return Collections.emptyMap();
//    }
//
//    public String getDisplayName(){
//        return property.getDisplayName();
//    }
}
