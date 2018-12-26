package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;


public class QueryServiceMacroTest extends QueryBe5ProjectDBTest
{
    @Inject
    private Meta meta;
    @Inject
    private DbService db;
    @Inject
    private QueryExecutorFactory queryService;

    @Before
    public void insertOneRow()
    {
        db.update("delete from testtable");
    }

    @Test
    public void testBeSqlMacros()
    {
        Query query = meta.getQuery("testtable", "testBeSqlMacros");
//        assertEquals("SELECT name || ' test', value ||' test'\n" +
//                "FROM testtable", db.format(query.getQueryCompiled().validate()));
        assertEquals("SELECT name || ' test', value ||' test'\n" +
                "FROM testtable", db.format(query.getQuery()));
        //assertEquals("", db.format(query.getQuery()));
    }

    @Test
    public void testFreemarkerMacros()
    {
        Query query = meta.getQuery("testtable", "testFreemarkerMacros");
        assertEquals("SELECT name || ' test', value || ' test'\n" +
                "FROM testtable", db.format(query.getQueryCompiled().validate()));
    }
}
