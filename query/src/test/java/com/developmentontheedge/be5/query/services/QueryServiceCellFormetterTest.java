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

    @Before
    public void insertOneRow()
    {
        setStaticUserInfo(RoleType.ROLE_GUEST);
        db.update("delete from testtable");
        db.insert("insert into testtable (name, valueCol) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testtable (name, valueCol) VALUES (?, ?)", "user2", 2L);

        db.update("delete from testSubQuery");
        db.insert("insert into testSubQuery (name, valueCol) VALUES (?, ?)", "user1", 1L);
        db.insert("insert into testSubQuery (name, valueCol) VALUES (?, ?)", "user1", 2L);
    }

    @Test
    public void subQuery()
    {
        Query query = meta.getQuery("testtable", "Sub Query");
        List<QRec> recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("1<br/>2", recs.get(0).getString("testSubQueryValues"));
    }

    /** No l10n.
     *  */
    @Test
    public void dictionaryLocalization()
    {
        Query query = meta.getQuery("testDictionary", "*** Selection view ***");
        List<QRec> recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("1", recs.get(0).getString("ID"));
        assertEquals("value1", recs.get(0).getString("CODE"));
        assertEquals("2", recs.get(1).getString("ID"));
        assertEquals("value2", recs.get(1).getString("CODE"));

        query = meta.getQuery("testTags", "dictionaryLocalization");
        recs = queryService.get(query, new HashMap<>()).execute();

        assertEquals("01101a", recs.get(0).getString("referenceTest"));
        assertEquals("01201a", recs.get(1).getString("referenceTest"));
        assertEquals("02201a", recs.get(2).getString("referenceTest"));
        assertEquals("02101a", recs.get(3).getString("referenceTest"));
    }

}
