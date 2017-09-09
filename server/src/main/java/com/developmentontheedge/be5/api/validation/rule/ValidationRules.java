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

    public static ValidationRules baseRule(BaseRule ruleNames)
    {
        return new ValidationRules("baseRule", ruleNames.name());
    }

    public static ValidationRules range(int from, int to)
    {
        return new ValidationRules("range", new Range(from, to));
    }

    public static ValidationRules unique(String tableName)
    {
        return new ValidationRules("unique", new Unique(tableName));
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

    public static class Range
    {
        int from,to;

        Range(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }
    }

    public static class Unique
    {
        String tableName;

        Unique(String tableName) {
            this.tableName = tableName;
        }

        public String getTableName() {
            return tableName;
        }
    }
}
