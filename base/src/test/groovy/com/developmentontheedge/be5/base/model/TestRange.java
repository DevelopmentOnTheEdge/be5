package com.developmentontheedge.be5.base.model;

public class TestRange
{
    private String min,max;

    TestRange(String min, String max) {
        this.min = min;
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }
}
