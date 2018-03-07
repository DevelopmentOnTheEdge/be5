package com.developmentontheedge.be5.api.validation.rule;

import com.developmentontheedge.beans.json.JsonFactory;


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

    public static ValidationRules range(long from, long to)
    {
        return new ValidationRules("range", new Range(from, to));
    }

    public static ValidationRules range(double from, double to)
    {
        return new ValidationRules("range", new DoubleRange(from, to));
    }

    public static ValidationRules step(long value)
    {
        return new ValidationRules("step", value);
    }

    public static ValidationRules step(double value)
    {
        return new ValidationRules("step", value);
    }

    public static ValidationRules pattern(String value)
    {
        return new ValidationRules("pattern", value);
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
        long from,to;

        Range(long from, long to) {
            this.from = from;
            this.to = to;
        }

        public long getFrom() {
            return from;
        }

        public long getTo() {
            return to;
        }
    }

    public static class DoubleRange
    {
        double from, to;

        DoubleRange(double from, double to) {
            this.from = from;
            this.to = to;
        }

        public double getFrom() {
            return from;
        }

        public double getTo() {
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
