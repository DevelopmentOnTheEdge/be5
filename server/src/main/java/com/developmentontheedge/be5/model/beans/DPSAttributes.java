package com.developmentontheedge.be5.model.beans;

import com.developmentontheedge.beans.BeanInfoConstants;

import java.util.HashMap;
import java.util.Map;

public class DPSAttributes
{
    private Map<String, Object> map = new HashMap<>();

    private String name;
    private Class<?> TYPE = String.class;

    public void setName(String value)
    {
        name = value;
    }

    public void setDISPLAY_NAME(String value)
    {
        map.put(BeanInfoConstants.DISPLAY_NAME, value);
    }
    public void setTYPE(Class<?> value)
    {
        TYPE = value;
    }
    public void setValue(Object value)
    {
        map.put("value", value);
    }
    public void setREAD_ONLY(boolean value)
    {
        map.put(BeanInfoConstants.READ_ONLY, value);
    }
    public void setHIDDEN(boolean value)
    {
        map.put(BeanInfoConstants.HIDDEN, value);
    }
    public void setRAW_VALUE(boolean value)
    {
        map.put(BeanInfoConstants.RAW_VALUE, value);
    }
    public void setRELOAD_ON_CHANGE(boolean value)
    {
        map.put(BeanInfoConstants.RELOAD_ON_CHANGE, value);
    }
    public void setRELOAD_ON_FOCUS_OUT(boolean value)
    {
        map.put(BeanInfoConstants.RELOAD_ON_FOCUS_OUT, value);
    }
    public void setCAN_BE_NULL(boolean value)
    {
        map.put(BeanInfoConstants.CAN_BE_NULL, value);
    }
    public void setMULTIPLE_SELECTION_LIST(boolean value)
    {
        map.put(BeanInfoConstants.MULTIPLE_SELECTION_LIST, value);
    }
    public void setPASSWORD_FIELD(boolean value)
    {
        map.put(BeanInfoConstants.PASSWORD_FIELD, value);
    }
    public void setLABEL_FIELD(boolean value)
    {
        map.put(BeanInfoConstants.LABEL_FIELD, value);
    }
    public void setTAG_LIST_ATTR(Object value)
    {
        map.put(BeanInfoConstants.TAG_LIST_ATTR, value);
    }
    public void setEXTRA_ATTRS(Object value)
    {
        map.put(BeanInfoConstants.EXTRA_ATTRS, value);
    }
    public void setGROUP_NAME(String value)
    {
        map.put(BeanInfoConstants.GROUP_NAME, value);
    }
    public void setGROUP_ID(Object value)
    {
        map.put(BeanInfoConstants.GROUP_ID, value);
    }
    public void setVALIDATION_RULES(Object value)
    {
        map.put(BeanInfoConstants.VALIDATION_RULES, value);
    }
    public void setCOLUMN_SIZE_ATTR(Object value)
    {
        map.put(BeanInfoConstants.COLUMN_SIZE_ATTR, value);
    }
    public void setDEFAULT_VALUE(Object value)
    {
        map.put(BeanInfoConstants.DEFAULT_VALUE, value);
    }
    public void setCSS_CLASSES(String value)
    {
        map.put(BeanInfoConstants.CSS_CLASSES, value);
    }
    public void setSTATUS(String value)
    {
        map.put(BeanInfoConstants.STATUS, value);
    }
    public void setMESSAGE(String value)
    {
        map.put(BeanInfoConstants.MESSAGE, value);
    }

    public Map<String, Object> getMap()
    {
        return map;
    }

    public String getName()
    {
        return name;
    }

    public Class<?> getTYPE()
    {
        return TYPE;
    }
}
