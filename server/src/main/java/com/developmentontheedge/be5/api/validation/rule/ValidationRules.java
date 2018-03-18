package com.developmentontheedge.be5.api.validation.rule;

import com.developmentontheedge.beans.json.JsonFactory;


public class ValidationRules
{
    public static Rule range(long min, long max)
    {
        return new Rule("range", new Range(min, max));
    }

    public static Rule range(double min, double max)
    {
        return new Rule("range", new DoubleRange(min, max));
    }

    public static Rule range(long min, long max, String customMessage)
    {
        return new Rule("range", new Range(min, max), customMessage);
    }

    public static Rule range(double min, double max, String customMessage)
    {
        return new Rule("range", new DoubleRange(min, max), customMessage);
    }

    public static Rule step(long value)
    {
        return new Rule("step", value);
    }

    public static Rule step(double value)
    {
        return new Rule("step", value);
    }

    public static Rule step(long value, String customMessage)
    {
        return new Rule("step", value, customMessage);
    }

    public static Rule step(double value, String customMessage)
    {
        return new Rule("step", value, customMessage);
    }

    public static Rule pattern(String value)
    {
        return new Rule("pattern", value);
    }

    public static Rule pattern(String value, String customMessage)
    {
        return new Rule("pattern", value, customMessage);
    }

    public static Rule unique(String tableName)
    {
        return new Rule("unique", new Unique(tableName));
    }

    public static Rule unique(String tableName, String customMessage)
    {
        return new Rule("unique", new Unique(tableName), customMessage);
    }

    public static class Rule
    {
        private String type;
        private Object attr;
        private String customMessage;

        Rule(String type, Object attr)
        {
            this.type = type;
            this.attr = attr;
        }

        Rule(String type, Object attr, String customMessage)
        {
            this.type = type;
            this.attr = attr;
            this.customMessage = customMessage;
        }

        public String getType()
        {
            return type;
        }

        public Object getAttr()
        {
            return attr;
        }

        public String getCustomMessage()
        {
            return customMessage;
        }

        @Override
        public String toString()
        {
            return JsonFactory.jsonb.toJson(this, this.getClass());
        }
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
        String columnName;

        Unique(String tableName) {
            this.tableName = tableName;
        }

        Unique(String tableName, String columnName)
        {
            this.tableName = tableName;
            this.columnName = columnName;
        }

        public String getTableName() {
            return tableName;
        }

        public String getColumnName()
        {
            return columnName;
        }
    }
}
