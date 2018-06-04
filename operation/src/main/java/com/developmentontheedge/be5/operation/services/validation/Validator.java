package com.developmentontheedge.be5.operation.services.validation;

import com.developmentontheedge.be5.base.exceptions.Be5Exception;
import com.developmentontheedge.be5.base.services.UserAwareMeta;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

import javax.inject.Inject;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;

import static com.developmentontheedge.be5.operation.services.validation.Validation.Status.ERROR;
import static com.developmentontheedge.be5.operation.services.validation.Validation.Status.SUCCESS;


public class Validator
{
    private final UserAwareMeta userAwareMeta;

    @Inject
    public Validator(UserAwareMeta userAwareMeta)
    {
        this.userAwareMeta = userAwareMeta;
    }

//    private Map<String, String> getValidationAttributes(DynamicProperty property)
//    {
//        Map<String, String> result = new LinkedHashMap<String, String>();
//        for( AbstractRule rule : defaultRules )
//        {
//            if( rule.isApplicable( property ) )
//            {
//                result.put( rule.getRule(), userAwareMeta.getLocalizedValidationMessage( rule.getMessage() ) );
//            }
//        }
//        return result;
//    }

    public void checkErrorAndCast(Object parameters)
    {
        isError(parameters);

        if (parameters instanceof DynamicPropertySet)
        {
            for (DynamicProperty property : (DynamicPropertySet)parameters)
            {
                checkErrorAndCast(property);
            }
        }
    }

    public void checkErrorAndCast(DynamicProperty property)
    {
        if(property.getValue() == null
                || ( property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST)
                     && ((Object[]) property.getValue()).length == 0))
        {
            if (property.isCanBeNull())
            {
                return;
            }
            else
            {
                String msg = userAwareMeta.getLocalizedValidationMessage("This field is required.");
                setError(property, msg);
                throw new IllegalArgumentException(msg + toStringProperty(property));
            }
        }

        if(property.getBooleanAttribute(BeanInfoConstants.MULTIPLE_SELECTION_LIST))
        {
            if(!(property.getValue() instanceof Object[]))
            {
                setError(property, "Value must be array (MULTIPLE_SELECTION_LIST)");
                throw Be5Exception.internal("Value must be array (MULTIPLE_SELECTION_LIST)" + toStringProperty(property));
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
                throw new IllegalArgumentException(msg + toStringProperty(property));
            }

            checkValueInTags(property, property.getValue());
        }

    }

    private void checkValueInTags(DynamicProperty property, Object value)
    {
        Object[][] tags = (Object[][])property.getAttribute(BeanInfoConstants.TAG_LIST_ATTR);
        if(tags != null)
        {
            if(Arrays.stream(tags).noneMatch(item -> (item)[0].toString().equals(value.toString())))
            {
                setError(property, "Value is not contained in tags");
                throw new IllegalArgumentException("Value is not contained in tags" + toStringProperty(property));
            }
        }
    }

    public void setSuccess(DynamicProperty property)
    {
        property.setAttribute( BeanInfoConstants.STATUS, SUCCESS.toString() );
        property.setAttribute( BeanInfoConstants.MESSAGE, "" );
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
            throw new NumberFormatException(msg + toStringProperty(property));
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
            throw new NumberFormatException(msg + toStringProperty(property));
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
            throw new IllegalArgumentException(msg + toStringProperty(property));
        }

        return value;
    }

    public void isError(Object parameters)
    {
        if (parameters instanceof DynamicPropertySet)
        {
            for (DynamicProperty property : (DynamicPropertySet)parameters)
            {
                if (isError(property))
                {
                    throw new IllegalArgumentException(property.getAttribute("message") + toStringProperty(property));
                }
            }
        }
    }

    public boolean isError(DynamicProperty property)
    {
        return property.getAttribute(BeanInfoConstants.STATUS) != null &&
                Validation.Status.valueOf(((String) property.getAttribute(BeanInfoConstants.STATUS)).toUpperCase())
                        == Validation.Status.ERROR;
    }

    private String toStringProperty(DynamicProperty property)
    {
        return "";
//        if(!UserInfoHolder.isSystemDeveloper())return "";
//        String value;
//        if(property.getValue() != null)
//        {
//            value = property.getValue().getClass().isArray() ? Arrays.toString((Object[]) property.getValue()) : property.getValue().toString();
//            value += " (" + property.getValue().getClass().getSimpleName() + ")";
//        }
//        else
//        {
//            value = "null";
//        }
//        return " - ["
//                + " name: '"  + property.getName()
//                + "', type: "  + property.getType()
//                + ", value: " + value
//                + " ]";
    }
}
