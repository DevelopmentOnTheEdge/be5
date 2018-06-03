package com.developmentontheedge.sql.model;

import org.junit.Test;

import static org.junit.Assert.*;


public class AstStringConstantTest
{
    @Test
    public void equals()
    {
        AstStringConstant t1 = new AstStringConstant("test");
        AstStringConstant t2 = new AstStringConstant(new AstStringPart("test"));

        assertEquals(t1, t2);
    }
}