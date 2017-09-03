package com.developmentontheedge.be5.api.validation.rule;

import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;


public class ValidationRules
{
    private String type;
    private Object attr;

    private ValidationRules(String type, Object attr)
    {
        this.type = type;
        this.attr = attr;
    }

    public static ValidationRules simpleRule(SimpleRule ruleNames)
    {
        return new ValidationRules("simpleRule", ruleNames.name());
    }

    public static ValidationRules range(int from, int to)
    {
        return new ValidationRules("range", ImmutableList.of(from, to));
    }

    public static ValidationRules unique(String tableName)
    {
        return new ValidationRules("unique", ImmutableMap.of("tableName", tableName));
    }

    public String getType()
    {
        return type;
    }

    public Object getAttr()
    {
        return attr;
    }

    @Override
    public String toString()
    {
        return JsonFactory.jsonb.toJson(this, this.getClass());
    }
}
