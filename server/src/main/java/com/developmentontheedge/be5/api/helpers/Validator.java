package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;


//TODO localize BeanInfoConstants.MESSAGE
public class Validator
{
    public enum Status
    {
        SUCCESS, WARNING, ERROR
    }

    public static void checkAndCast(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps) checkAndCast(property);
    }

    public static void checkAndCast(DynamicProperty property)
    {
        if(property.getValue() instanceof String && property.getType() != String.class)
        {
            try
            {
                property.setValue(getTypedValueFromString(property, property.getValue().toString()));
            }
            catch (IllegalArgumentException e)
            {
                setState(property, Status.ERROR, e);
            }
        }
        else
        {
            if (property.getValue() == null || property.getType() != property.getValue().getClass())
            {
                setState(property, Status.ERROR, new IllegalArgumentException());
            }
        }
    }

    public static void setState(DynamicProperty property, Status status)
    {
        setState(property, status, "");
    }

    public static void setState(DynamicProperty property, Status status, Throwable e)
    {
        if(e.getClass() == NumberFormatException.class)
            setState(property, status, "Error, value must be a " + property.getType().getName());
        else setState(property, status, e.getMessage());
    }

    public static void setState(DynamicProperty property, Status status, String message)
    {
        property.setAttribute( BeanInfoConstants.STATUS, status.toString().toLowerCase() );
        if(message != null && !message.isEmpty())
        {
            property.setAttribute( BeanInfoConstants.MESSAGE, message );
        }
    }

    private static Object getTypedValueFromString(DynamicProperty property, String value)
    {
        Class<?> type = property.getType();

        if (type == Integer.class)
        {
            return Integer.parseInt(value);
        }
        if (type == Long.class)
        {
            return Long.parseLong(value);
        }
        if (type == Float.class)
        {
            return Float.parseFloat(value);
        }
        if (type == Double.class)
        {
            return Double.parseDouble(value);
        }
        if (type == Boolean.class)
        {
            return Boolean.parseBoolean(value);
        }

        return value;
    }

    public static boolean isError(DynamicProperty property)
    {
        return property.getAttribute(BeanInfoConstants.STATUS) != null &&
                Validator.Status.valueOf(((String) property.getAttribute(BeanInfoConstants.STATUS)).toUpperCase())
                        == Validator.Status.ERROR;
    }

    public static void replaceNullValueToStr(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps){
            if(property.getValue() == null){
                property.setValue("");
            }
        }
    }

}
