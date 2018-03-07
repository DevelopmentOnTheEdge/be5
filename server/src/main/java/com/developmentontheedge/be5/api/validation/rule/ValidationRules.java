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

    public static ValidationRules range(long min, long max)
    {
        return new ValidationRules("range", new Range(min, max));
    }

    public static ValidationRules range(double min, double max)
    {
        return new ValidationRules("range", new DoubleRange(min, max));
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
        long min,max;

        Range(long min, long max) {
            this.min = min;
            this.max = max;
        }

        public long getMin() {
            return min;
        }

        public long getMax() {
            return max;
        }
    }

    public static class DoubleRange
    {
        double min, max;

        DoubleRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
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
