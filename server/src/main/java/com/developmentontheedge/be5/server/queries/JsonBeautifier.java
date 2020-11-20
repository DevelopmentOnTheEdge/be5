package com.developmentontheedge.be5.server.queries;

import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;
import com.developmentontheedge.be5.query.model.beans.QRec;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JsonBeautifier implements SubQueryBeautifier
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String print(List<QRec> lists)
    {
        if(lists == null || lists.size() == 0 )
            return "";

        List<Map<String, Object>> mapList = new ArrayList<>(lists.size());
        for(int i = 0; i<lists.size(); i++)
        {
            mapList.add( lists.get(i).asMap());
        }

        return jsonb.toJson(mapList);
    }
}
