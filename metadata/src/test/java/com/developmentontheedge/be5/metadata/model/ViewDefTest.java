package com.developmentontheedge.be5.metadata.model;

import com.developmentontheedge.be5.metadata.sql.Rdbms;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ViewDefTest
{
    private Project prj;
    private Entity e;

    @Before
    public void setUp()
    {
        prj = new Project("test");
        e = new Entity("e", prj.getApplication(), EntityType.TABLE);
    }

    @Test
    public void testFormatForH2()
    {
        prj.setDatabaseSystem(Rdbms.H2);
        ViewDef viewDef = new ViewDef(e);
        viewDef.setDefinition("SELECT CONCAT('persons.', id) AS \"newID\" FROM t");
        assertEquals("SELECT CONCAT('persons.', id) AS \"NEWID\" FROM t", viewDef.getDefinition());
    }
}
