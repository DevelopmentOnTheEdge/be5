package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.ProjectProvider;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.Be5ProjectDBTest;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TableModelTest extends Be5ProjectDBTest
{
    @Inject private SqlService db;
    @Inject private ProjectProvider projectProvider;
    @Inject private Injector injector;

    @Test
    public void testExecuteSubQuery() {
        if(db.getLong("select count(*) from testtUser") == 0){
            db.insert("insert into testtable (name, value) VALUES (?, ?)",
                    "tableModelTest", "1");
            db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user1");
            db.insert("insert into testtUser (name, value) VALUES (?, ?)","tableModelTest", "user2");
        }

        Query query = projectProvider.getProject().getEntity("testtable").getQueries().get("Sub Query");
        TableModel table = TableModel
                .from(query, new HashMap<>(), mock(Request.class), false, injector)
                .limit(20)
                .build();


        assertEquals("user1; user2", table.getRows().get(0).getCells().get(2).content);
    }


}