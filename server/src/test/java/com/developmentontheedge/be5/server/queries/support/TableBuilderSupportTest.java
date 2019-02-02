package com.developmentontheedge.be5.server.queries.support;

import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class TableBuilderSupportTest extends ServerBe5ProjectTest
{
    @Inject
    private QueryExecutorFactory queryExecutorFactory;

    @Test
    public void test()
    {
        initGuest();
        Query query = meta.getQuery("testtableAdmin", "TestGroovyTable");
        List<QRec> rows = queryExecutorFactory.get(query, Collections.emptyMap()).execute();

        assertEquals("Guest", rows.get(0).getProperty("Guest").getDisplayName());
        assertEquals("[Guest]", rows.get(0).getValue("Guest"));
    }

}
