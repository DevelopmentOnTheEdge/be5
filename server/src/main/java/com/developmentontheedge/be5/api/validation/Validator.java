package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.developmentontheedge.be5.api.validation.Validation.Status.SUCCESS;
import static com.developmentontheedge.be5.api.validation.Validation.Status.ERROR;
import static com.developmentontheedge.be5.api.validation.Validation.defaultRules;


//TODO localize BeanInfoConstants.MESSAGE
public class Validator
{
    private final UserAwareMeta userAwareMeta;

    public Validator(UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
    }

    private Map<String, String> getValidationAttributes(DynamicProperty property)
    {
        Map<String, String> result = new LinkedHashMap<String, String>();
        for( AbstractRule rule : defaultRules )
        {
            if( rule.isApplicable( property ) )
            {
                result.put( rule.getRule(), userAwareMeta.getLocalizedValidationMessage( rule.getMessage() ) );
            }
        }
        return result;
    }

    public void checkErrorAndCast(DynamicPropertySet dps)
    {
        isError(dps);
        for (DynamicProperty property: dps)
        {
            checkErrorAndCast(property);
        }
    }

    public void checkErrorAndCast(DynamicProperty property)
    {
        if(property.getValue() instanceof String && property.getType() != String.class)
        {
            String stringValue = (String)property.getValue();
            if(stringValue.isEmpty() && property.isCanBeNull())
            {
                property.setValue(null);
            }
            else
            {
                property.setValue(parseFrom(property, stringValue));
            }
        }
        else
        {
            if(property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST))
            {
                if(!(property.getValue() instanceof Object[]))
                {
                    setError(property, "Value must be array (MULTIPLE_SELECTION_LIST)");
                    throw Be5Exception.internal("Value must be array (MULTIPLE_SELECTION_LIST) - " + property.toString());
                }

                Object[] values = (Object[]) property.getValue();
                Object[] resValues = new Object[values.length];

                for (int i = 0; i < values.length; i++)
                {
                    resValues[i] = parseFrom(property, (String) values[i]);
                }
                property.setValue(resValues);
            }
            else
            {
                if (property.getValue() == null)
                {
                    if(!property.isCanBeNull()){
                        setError(property, "Can not be null");
                        throw new IllegalArgumentException("Can not be null - " + property.toString());
                    }
                }
                else
                {
                    if (property.getType() != property.getValue().getClass())
                    {
                        String msg = "Error, value must be a " + property.getType().getName();
                        setError(property, msg);
                        throw new IllegalArgumentException(msg + " - " + property.toString());
                    }
                }
            }
        }
    }

    public void setSuccess(DynamicProperty property)
    {
        property.setAttribute( BeanInfoConstants.STATUS, SUCCESS.toString() );
    }

    public void setError(DynamicProperty property, String message)
    {
        property.setAttribute( BeanInfoConstants.STATUS, ERROR.toString() );
        property.setAttribute( BeanInfoConstants.MESSAGE, message );
    }

    @Deprecated
    public Object parseFrom(DynamicProperty property, String value)
    {
        if(value == null) return null;

        Class<?> type = property.getType();

        //todo move to IntegerRule, NumberRule
        try
        {
            if (type == Short.class) return Short.parseShort(value);
            if (type == Integer.class) return Integer.parseInt(value);
            if (type == Long.class) return Long.parseLong(value);
            if (type == Float.class) return Float.parseFloat(value);
            if (type == Double.class) return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            String msg = "Error, value must be a " + type.getName();
            setError(property, msg);
            throw new NumberFormatException(msg + " - " + property.toString());
        }

        if (type == Boolean.class)  return Boolean.parseBoolean(value);

        //todo move to DateRule
        try{
            if (type == Date.class)     return Date.valueOf(value);
            if (type == Timestamp.class)return Timestamp.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            String msg = "Error, value must be a " + type.getName();
            setError(property, msg);
            throw new IllegalArgumentException(msg + " - " + property.toString());
        }

        if (type == String.class)return value;

        //todo проверить в be3
        throw new IllegalArgumentException("Unknown type, Возможно тип был автоматически определён из массива(при MULTIPLE_SELECTION_LIST) - тогда вручную укажите тип этемента." + property.toString());
    }

    public void isError(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps)
        {
            if(isError(property))throw new IllegalArgumentException(property.toString());
        }
    }

    public boolean isError(DynamicProperty property)
    {
        return property.getAttribute(BeanInfoConstants.STATUS) != null &&
                Validation.Status.valueOf(((String) property.getAttribute(BeanInfoConstants.STATUS)).toUpperCase())
                        == Validation.Status.ERROR;
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
