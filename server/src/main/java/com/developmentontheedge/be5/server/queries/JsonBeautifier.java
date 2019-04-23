package com.developmentontheedge.be5.server.queries;

import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.List;
import java.util.Map;

public class JsonBeautifier implements SubQueryBeautifier
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String print(List<Map<String, Object>> lists)
    {
        return jsonb.toJson(lists);
    }
}
