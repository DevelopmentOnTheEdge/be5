package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.QueryExecutor;
import com.developmentontheedge.be5.query.model.beans.QRec;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class QueryServiceTest extends QueryBe5ProjectDBTest
{
    @Inject
    private DbService db;
    @Inject
    private QueryExecutorFactory queryService;
    @Inject
    private QueriesService queries;

    @Before
    public void insertOneRow()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
        db.update("delete from testtable");
        db.insert("insert into testtable (name, valueCol) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testtable (name, valueCol) VALUES (?, ?)", "user2", 2L);
    }

    @Test
    public void testExecute()
    {
        Query query = meta.getQuery("testtable", "All records");
        List<QRec> dps = queryService.get(query, emptyMap()).execute();
        assertTrue(dps.size() > 0);

        assertEquals(String.class, dps.get(0).getProperty("name").getType());
    }

    @Test
    public void testCountFromQuery()
    {
        QueryExecutor sqlQueryBuilder = queryService.get(meta.getQuery("testtable", "All records"), emptyMap());

        assertTrue(sqlQueryBuilder.count() > 0);
    }

    @Test
    public void beAggregate1D()
    {
        Query query = meta.getQuery("testtable", "beAggregate1D");
        List<QRec> recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("3.0", recs.get(2).getString("Value"));
    }

    @Test
    public void testIgnoreUnknownColumn()
    {
        Query query = meta.getQuery("testtable", "TestResolveRefColumn");
        List<QRec> list = queryService.get(query, of("unknownColumn", "test")).execute();
        assertEquals(list.size(), 0);
    }
}
