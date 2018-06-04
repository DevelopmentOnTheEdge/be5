package com.developmentontheedge.be5.metadata.sql.macro;

import com.developmentontheedge.be5.metadata.sql.Rdbms;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BeSQLMacroProcessorStrategyTest
{
    @Test
    public void testAsPk()
    {
        assertEquals("TO_KEY( test )", Rdbms.BESQL.getMacroProcessorStrategy().castAsPrimaryKey( "test" ));
    }
}
