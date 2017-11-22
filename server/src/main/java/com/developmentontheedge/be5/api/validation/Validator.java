package com.developmentontheedge.be5.api.validation;

import com.developmentontheedge.be5.api.exceptions.Be5Exception;
import com.developmentontheedge.be5.api.helpers.UserAwareMeta;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.developmentontheedge.be5.api.validation.Validation.Status.SUCCESS;
import static com.developmentontheedge.be5.api.validation.Validation.Status.ERROR;
import static com.developmentontheedge.be5.api.validation.Validation.defaultRules;


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
        if(property.getValue() instanceof String && ((String) property.getValue()).isEmpty())
        {
            property.setValue(null);
        }

        if(property.getValue() == null)
        {
            if (property.isCanBeNull())
            {
                return;
            }
            else
            {
                setError(property, userAwareMeta.getLocalizedValidationMessage("This field is required."));
                throw new IllegalArgumentException("This field is required. - " + toStringProperty(property));
            }
        }

        if(property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST))
        {
            if(!(property.getValue() instanceof Object[]))
            {
                setError(property, "Value must be array (MULTIPLE_SELECTION_LIST)");
                throw Be5Exception.internal("Value must be array (MULTIPLE_SELECTION_LIST) - " + toStringProperty(property));
            }

            Object[] values = (Object[]) property.getValue();
            Object[] resValues = new Object[values.length];

            for (int i = 0; i < values.length; i++)
            {
                if(values[i] instanceof String)
                    resValues[i] = parseFrom(property, (String)values[i]);
                else
                    resValues[i] = values[i];
                checkValueInTags(property, resValues[i]);
            }
            if(values.length == 0 && !property.isCanBeNull())
            {
                setError(property, userAwareMeta.getLocalizedValidationMessage("This field is required."));
                throw new IllegalArgumentException("This field is required. - " + toStringProperty(property));
            }
            property.setValue(resValues);
        }
        else
        {
            if(property.getValue() instanceof String && property.getType() != String.class)
            {
                property.setValue(parseFrom(property, (String) property.getValue()));
            }
            else if (property.getType() != property.getValue().getClass())
            {
                String msg = "Error, value must be a " + property.getType().getName();
                setError(property, msg);
                throw new IllegalArgumentException(msg + " - " + toStringProperty(property));
            }

            checkValueInTags(property, property.getValue());
        }

    }

    private void checkValueInTags(DynamicProperty property, Object value)
    {
        String[][] tags = (String[][])property.getAttribute(BeanInfoConstants.TAG_LIST_ATTR);
        if(tags != null)
        {
            if(Arrays.stream(tags).noneMatch(item -> (item)[0].equals(value.toString())))
            {
                setError(property, "Value is not contained in tags");
                throw new IllegalArgumentException("Value is not contained in tags - " + toStringProperty(property));
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
        }
        catch (NumberFormatException e)
        {
            String msg = userAwareMeta.getLocalizedValidationMessage("Please specify an integer number.");
            //добавить информацию о конкресном типе - ограничения type.getName();
            try{
                BigInteger bigInteger = new BigInteger(value);
                if(type == Long.class){
                    if(bigInteger.compareTo(BigInteger.ZERO) > 0){
                        msg += " <= " + Long.MAX_VALUE;
                    }else{
                        msg += " >= " + Long.MIN_VALUE;
                    }
                }
                if(type == Integer.class){
                    if(bigInteger.compareTo(BigInteger.ZERO) > 0){
                        msg += " <= " + Integer.MAX_VALUE;
                    }else{
                        msg += " >= " + Integer.MIN_VALUE;
                    }
                }
                if(type == Short.class){
                    if(bigInteger.compareTo(BigInteger.ZERO) > 0){
                        msg += " <= " + Short.MAX_VALUE;
                    }else{
                        msg += " >= " + Short.MIN_VALUE;
                    }
                }
            }catch (RuntimeException ignore){}

            setError(property, msg);
            throw new NumberFormatException(msg + " - " + toStringProperty(property));
        }

        try
        {
            if (type == Float.class) return Float.parseFloat(value);
            if (type == Double.class) return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            String msg = userAwareMeta.getLocalizedValidationMessage("Please enter a valid number.");
            //добавить информацию о конкресном типе - ограничения type.getName();
            setError(property, msg);
            throw new NumberFormatException(msg + " - " + toStringProperty(property));
        }

        if (type == Boolean.class)  return Boolean.parseBoolean(value);

        //todo move to DateRule
        try{
            if (type == Date.class)     return Date.valueOf(value);
            if (type == Timestamp.class)return Timestamp.valueOf(value);
        }
        catch (IllegalArgumentException e)
        {
            String msg = userAwareMeta.getLocalizedValidationMessage("Please enter a valid date.");
            setError(property, msg);
            throw new IllegalArgumentException(msg + " - " + toStringProperty(property));
        }

        if (type == String.class)return value;

        //todo проверить в be3
        throw new IllegalArgumentException("Unknown type, Возможно тип был автоматически определён из массива(при MULTIPLE_SELECTION_LIST) - тогда вручную укажите тип этемента." + toStringProperty(property));
    }

    public void isError(DynamicPropertySet dps)
    {
        for (DynamicProperty property: dps)
        {
            if(isError(property))throw new IllegalArgumentException(toStringProperty(property));
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

    private String toStringProperty(DynamicProperty property)
    {
        String value;
        if(property.getValue() != null)
        {
            value = property.getValue().getClass().isArray() ? Arrays.toString((Object[]) property.getValue()) : property.getValue().toString();
            value += " (" + property.getValue().getClass().getSimpleName() + ")";
        }
        else
        {
            value = "null";
        }
        return "["
                + " name: '"  + property.getName()
                + "', type: "  + property.getType()
                + ", value: " + value
                + " ]";
    }
}
