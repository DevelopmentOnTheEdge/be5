package com.developmentontheedge.be5.test;

import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;


public abstract class BaseTestUtils
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    protected static String oneQuotes(Object s)
    {
        return s.toString().replace("\"", "'");
    }

    protected static String doubleQuotes(Object s)
    {
        return s.toString().replace("'", "\"");
    }

    protected static DynamicPropertySetSupport getDpsS(Map<String, ?> nameValues)
    {
        return getDps(new DynamicPropertySetSupport(), nameValues);
    }

    protected static <T extends DynamicPropertySet> T getDps(T dps, Map<String, ?> nameValues)
    {
        for (Map.Entry<String, ?> entry : nameValues.entrySet())
        {
            dps.add(new DynamicProperty(entry.getKey(), entry.getValue().getClass(), entry.getValue()));
        }
        return dps;
    }

    protected Date parseDate(String stringDate)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            return new Date(df.parse(stringDate).getTime());
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

}
