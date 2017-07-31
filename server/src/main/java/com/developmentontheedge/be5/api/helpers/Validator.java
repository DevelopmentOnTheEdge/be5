package com.developmentontheedge.be5.api.helpers;

import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.sql.Date;

import static com.developmentontheedge.be5.api.helpers.Validator.Status.SUCCESS;
import static com.developmentontheedge.be5.api.helpers.Validator.Status.ERROR;


//TODO localize BeanInfoConstants.MESSAGE
public class Validator
{
    private final UserAwareMeta userAwareMeta;

    public Validator(Injector injector){
        userAwareMeta = UserAwareMeta.get(injector);
    }

    public enum Status
    {
        SUCCESS, WARNING, ERROR
    }

    public void checkErrorAndCast(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps)
        {
            checkErrorAndCast(property);
        }
    }

    public void checkErrorAndCast(DynamicProperty property)
    {
        try
        {
            if(property.getValue() instanceof String && property.getType() != String.class)
            {
                if(property.isCanBeNull() && ((String) property.getValue()).length() == 0){
                    property.setValue(null);
                }else{
                    property.setValue(getTypedValueFromString(property.getType(), property.getValue()));
                }
            }
            else
            {
                if(property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST))
                {
                    if(!(property.getValue() instanceof Object[]))
                    {
                        throw new IllegalArgumentException(property.toString());
                    }

                    Object[] values = (Object[]) property.getValue();
                    Object[] resValues = new Object[values.length];

                    for (int i = 0; i < values.length; i++)
                    {
                        resValues[i] = getTypedValueFromString(property.getType(), values[i]);
                    }
                    property.setValue(resValues);
                }
                else
                {
                    if (property.getValue() == null){
                        if(!property.isCanBeNull())throw new NullPointerException(property.toString() + " - can not be null");
                    }
                    else
                    {
                        if (property.getType() != property.getValue().getClass())
                        {
                            throw new IllegalArgumentException(property.toString() +
                                    " type must be " + property.getValue().getClass().toString());
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            setError(property, e);
            throw e;
        }
    }

    public void setSuccess(DynamicProperty property)
    {
        property.setAttribute( BeanInfoConstants.STATUS, SUCCESS.toString().toLowerCase() );
    }

    public void setError(DynamicProperty property, Throwable e)
    {
        property.setAttribute( BeanInfoConstants.STATUS, ERROR.toString().toLowerCase() );

        String msg = "Error";
        if(e instanceof IllegalArgumentException)msg = "Error, value must be a " + property.getType().getName();

        property.setAttribute( BeanInfoConstants.MESSAGE, msg);
    }

    public void setError(DynamicProperty property, String message)
    {
        property.setAttribute( BeanInfoConstants.STATUS, ERROR.toString().toLowerCase() );
        property.setAttribute( BeanInfoConstants.MESSAGE, message );
    }

    private Object getTypedValueFromString(Class<?> type, Object value)
    {
        if(value instanceof String)
        {
            if (type == Integer.class)
            {
                return Integer.parseInt(value.toString());
            }
            if (type == Long.class)
            {
                return Long.parseLong(value.toString());
            }
            if (type == Float.class)
            {
                return Float.parseFloat(value.toString());
            }
            if (type == Double.class)
            {
                return Double.parseDouble(value.toString());
            }
            if (type == Boolean.class)
            {
                return Boolean.parseBoolean(value.toString());
            }
            if (type == Date.class)
            {
                return Date.valueOf(value.toString());
            }
        }

        return value;
    }

    public boolean isError(DynamicProperty property)
    {
        return property.getAttribute(BeanInfoConstants.STATUS) != null &&
                Validator.Status.valueOf(((String) property.getAttribute(BeanInfoConstants.STATUS)).toUpperCase())
                        == Validator.Status.ERROR;
    }

    public void replaceNullValueToEmptyString(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps){
            if(property.getValue() == null){
                property.setValue("");
            }
        }
    }

}
