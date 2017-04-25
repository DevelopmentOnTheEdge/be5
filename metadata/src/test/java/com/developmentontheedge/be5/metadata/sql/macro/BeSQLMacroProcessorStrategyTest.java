package com.developmentontheedge.be5.metadata.sql.macro;

import static org.junit.Assert.*;

import org.junit.Test;

import com.developmentontheedge.be5.metadata.sql.Rdbms;

public class BeSQLMacroProcessorStrategyTest
{
    @Test
    public void testAsPk()
    {
        assertEquals("TO_KEY( test )", Rdbms.BESQL.getMacroProcessorStrategy().castAsPrimaryKey( "test" ));
    }
}
