package com.developmentontheedge.be5.server.queries;

import com.developmentontheedge.be5.query.impl.beautifiers.SubQueryBeautifier;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.util.Utils;
import com.developmentontheedge.beans.DynamicProperty;
import one.util.streamex.StreamEx;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JsonBeautifier implements SubQueryBeautifier
{
    protected static final Jsonb jsonb = JsonbBuilder.create();

    @Override
    public String print(List<QRec> lists)
    {
        List<Map<String, Object>> mapList = lists.stream()
                .map(this::toRow)
                .collect(Collectors.toList());

        return jsonb.toJson(mapList);
    }

    private Map<String, Object> toRow(QRec qRec)
    {
        return StreamEx.of(qRec.spliterator())
                .collect(Utils.toLinkedMap(DynamicProperty::getName, DynamicProperty::getValue));
    }
}
