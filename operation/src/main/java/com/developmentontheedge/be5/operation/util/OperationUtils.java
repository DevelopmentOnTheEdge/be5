package com.developmentontheedge.be5.operation.util;

import java.util.HashMap;
import java.util.Map;

public class OperationUtils
{
    public static String[] selectedRows(String selectedRowsString)
    {
        if(selectedRowsString.trim().isEmpty())return new String[0];
        return selectedRowsString.split(",");
    }

    public static Map<String, Object> replaceEmptyStringToNull(Map<String, Object> values)
    {
        HashMap<String, Object> map = new HashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            if( "".equals(entry.getValue()) )
            {
                map.put(entry.getKey(), null);
            }
            else
            {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
}