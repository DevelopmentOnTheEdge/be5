package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.OperationDBTest;
import com.developmentontheedge.be5.test.TestUtils;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StandardOperationsDBTest extends OperationDBTest
{
    private Long id;

    @Before
    public void addRecords()
    {
        db.update("DELETE FROM testtableAdmin");
        id = db.insert("INSERT INTO testtableAdmin (name, value) VALUES (?, ?)", "TestName", 1);
    }

    @Test
    public void editOperationGenerate()
    {
        Object first = generateOperation("testtableAdmin", "All records", "Edit", id.toString(),"{}").getFirst();

        assertEquals("{'name':'TestName','value':'1'}",
        oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        OperationResult operationResult = executeOperation("testtableAdmin", "All records", "Edit", id.toString(),
                "{'name':'EditName','value':123}").getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        assertEquals("EditName,123",
                db.select("SELECT name, value FROM testtableAdmin WHERE id = ?", TestUtils::resultSetToString, id));
    }

    @Test
    public void deleteInvoke()
    {
        database.getEntity("testCollection").add(ImmutableMap.of(
                "categoryID", id.toString()
        ));

        database.getEntity("testGenCollection").add(ImmutableMap.of(
                "recordID", "testtableAdmin." + id.toString(),
                "categoryID", 123123L
        ));

        generateOperation("testtableAdmin", "All records", "Delete",
                id.toString(), "").getSecond();

        assertEquals(0, database.getEntity("testCollection").count());
        assertEquals(0, database.getEntity("testGenCollection").count());
    }

}