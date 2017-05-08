package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.AbstractProjectTestH2DB;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TableModelTest extends AbstractProjectTestH2DB
{
    private static SqlService db = sp.getSqlService();

    @Test
    public void testExecuteSubQuery() {
        if((Long)db.selectScalar("select count(*) from testtUser") == 0){
            db.insert("insert into testtable (name, value) VALUES (?, ?)",
                    "tableModelTest", "1");
            db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user1");
            db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user2");
        }

        Query query = sp.getProject().getEntity("testtable").getQueries().get("Sub Query");
        TableModel table = TableModel
                .from(query, new HashMap<>(), mock(Request.class), false)
                .limit(20)
                .build();


        assertEquals("user1; user2", table.getRows().get(0).getCells().get(2).content);
    }


}