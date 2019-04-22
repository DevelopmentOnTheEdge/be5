package com.developmentontheedge.be5.query.services;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.be5.query.model.beans.QRec;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class QueryServiceCellFormetterTest extends QueryBe5ProjectDBTest
{
    @Inject
    private QueryExecutorFactory queryService;

    private Long user1ID;
    private Long user2ID;

    @Before
    public void insertOneRow()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
        db.update("delete from testtable");
        user1ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user1", 1L);
        user2ID = db.insert("insert into testtable (name, value) VALUES (?, ?)", "user2", 2L);

        db.update("delete from testSubQuery");
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testSubQuery (name, value) VALUES (?, ?)", "user1", 2L);
    }

    @Test
    public void subQuery()
    {
        Query query = meta.getQuery("testtable", "Sub Query");
        List<QRec> recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("1<br/>2", recs.get(0).getString("testSubQueryValues"));
    }

    @Test
    public void dictionaryLocalization()
    {
        Query query = meta.getQuery("testTags", "dictionaryLocalization");
        List<QRec> recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("Региональный", recs.get(0).getString("referenceTest"));
        assertEquals("Региональный", recs.get(1).getString("referenceTest"));
        assertEquals("Муниципальный", recs.get(2).getString("referenceTest"));
        assertEquals("Муниципальный", recs.get(3).getString("referenceTest"));
    }

}
