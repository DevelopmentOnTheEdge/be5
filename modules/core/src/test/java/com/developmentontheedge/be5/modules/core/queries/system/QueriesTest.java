package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest;
import com.developmentontheedge.be5.database.QRec;
import com.developmentontheedge.be5.query.services.QueryExecutorFactory;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class QueriesTest extends CoreBe5ProjectDBTest
{
    @Inject
    private QueryExecutorFactory queryExecutorFactory;

    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @Test
    public void getEntities()
    {
        Query query = meta.getQuery("_system_", "Entities");

        List<QRec> recs = queryExecutorFactory.get(query, Collections.emptyMap()).execute();

        assertTrue(recs.stream()
                .anyMatch(x -> "_system_".equals(x.getValue("Name"))));
    }

    @Test
    public void getSessionVariables()
    {
        session.set("test", "value");
        Query query = meta.getQuery("_system_", "Session variables");

        List<QRec> recs = queryExecutorFactory.get(query, Collections.emptyMap()).execute();

        assertTrue(recs.stream()
                .map(x -> x.getValue("___ID"))
                .anyMatch(x -> x.equals("test")));

//        assertEquals("test", ((TableModel.CellModel)table.getRows().get(0).cells.get(0)).content)
//        assertEquals("value", ((TableModel.CellModel)table.getRows().get(0).cells.get(1)).content)
    }

}
