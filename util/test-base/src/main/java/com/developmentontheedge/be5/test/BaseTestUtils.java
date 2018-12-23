package com.developmentontheedge.be5.test;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.test.mocks.DbServiceMock;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.common.collect.ImmutableMap;
import org.mockito.Matchers;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.when;


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

    public static DynamicPropertySetSupport getDpsS(Map<String, ?> nameValues)
    {
        return getDps(new DynamicPropertySetSupport(), nameValues);
    }

    public static <T extends DynamicPropertySet> T getDps(T dps, Map<String, ?> nameValues)
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

    public static void whenSelectListTagsContains(String containsSql, String... tagValues)
    {
        List<DynamicPropertySet> tagValuesList = Arrays.stream(tagValues)
                .map(tagValue -> getDpsS(ImmutableMap.of("CODE", tagValue, "Name", tagValue)))
                .collect(Collectors.toList());

        when(DbServiceMock.mock.list(contains(containsSql),
                Matchers.<ResultSetParser<DynamicPropertySet>>any(), anyVararg())).thenReturn(tagValuesList);
    }
}
