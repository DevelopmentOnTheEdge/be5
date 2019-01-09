package com.developmentontheedge.be5.operation.util;

import com.developmentontheedge.be5.operation.OperationStatus;

import java.util.LinkedHashMap;
import java.util.Map;

public class OperationUtils
{
    public static String[] selectedRows(String selectedRowsString)
    {
        if (selectedRowsString == null || selectedRowsString.trim().isEmpty()) return new String[0];
        return selectedRowsString.split(",");
    }

    public static boolean operationSuccessfullyFinished(OperationStatus operationStatus)
    {
        return operationStatus == OperationStatus.FINISHED ||
                operationStatus == OperationStatus.REDIRECTED;
    }

    public static Map<String, Object> replaceEmptyStringToNull(Map<String, Object> values)
    {
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            if ("".equals(entry.getValue()))
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
