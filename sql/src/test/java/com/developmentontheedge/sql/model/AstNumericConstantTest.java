package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class AstNumericConstantTest
{
    @Test
    public void equals()
    {
        AstNumericConstant t1 = new AstNumericConstant((Number)1);
        AstNumericConstant t2 = new AstNumericConstant(1L);

        assertNotEquals(t1, t2);
    }
}