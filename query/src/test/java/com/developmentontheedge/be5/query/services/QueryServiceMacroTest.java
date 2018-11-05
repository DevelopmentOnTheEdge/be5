package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.base.services.Meta;
import com.developmentontheedge.be5.base.services.ProjectProvider;
import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

import static org.junit.Assert.assertEquals;


public class QueryServiceMacroTest extends QueryBe5ProjectDBTest
{
    @Inject
    private Meta meta;
    @Inject
    private DbService db;
    @Inject
    private QueryService queryService;

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

    @Test
    public void testTime()
    {
        int count = 1;
        Query query = meta.getQuery("testtable", "testBeSqlMacros");
        Query query2 = meta.getQuery("testtable", "testFreemarkerMacros");
        long start;
        for (int j = 0; j < 5; j++)
        {
            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++)
            {
                query2.getQueryCompiled().validate();
            }
            long diff = (System.currentTimeMillis() - start);
            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++)
            {
                db.format(query.getQuery());
            }
            long diff2 = (System.currentTimeMillis() - start);
            start = System.currentTimeMillis();
            for (int i = 0; i < count; i++)
            {
                queryService.build(query).getFinalSql().format();
            }
            System.out.println(diff + "\t " + diff2 + "\t " + (System.currentTimeMillis() - start));
        }
    }
}
