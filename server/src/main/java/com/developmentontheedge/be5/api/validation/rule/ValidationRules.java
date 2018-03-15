package com.developmentontheedge.be5.api.validation.rule;

import com.developmentontheedge.beans.json.JsonFactory;


public class ValidationRules
{
    public static ValidationRule range(long min, long max)
    {
        return new ValidationRule("range", new Range(min, max));
    }

    public static ValidationRule range(double min, double max)
    {
        return new ValidationRule("range", new DoubleRange(min, max));
    }

    public static ValidationRule range(long min, long max, String customMessage)
    {
        return new ValidationRule("range", new Range(min, max), customMessage);
    }

    public static ValidationRule range(double min, double max, String customMessage)
    {
        return new ValidationRule("range", new DoubleRange(min, max), customMessage);
    }

    public static ValidationRule step(long value)
    {
        return new ValidationRule("step", value);
    }

    public static ValidationRule step(double value)
    {
        return new ValidationRule("step", value);
    }

    public static ValidationRule step(long value, String customMessage)
    {
        return new ValidationRule("step", value, customMessage);
    }

    public static ValidationRule step(double value, String customMessage)
    {
        return new ValidationRule("step", value, customMessage);
    }

    public static ValidationRule pattern(String value)
    {
        return new ValidationRule("pattern", value);
    }

    public static ValidationRule pattern(String value, String customMessage)
    {
        return new ValidationRule("pattern", value, customMessage);
    }

    public static ValidationRule unique(String tableName)
    {
        return new ValidationRule("unique", new Unique(tableName));
    }

    public static ValidationRule unique(String tableName, String customMessage)
    {
        return new ValidationRule("unique", new Unique(tableName), customMessage);
    }

    public static class ValidationRule
    {
        private String type;
        private Object attr;
        private String customMessage;

        ValidationRule(String type, Object attr)
        {
            this.type = type;
            this.attr = attr;
        }

        ValidationRule(String type, Object attr, String customMessage)
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
