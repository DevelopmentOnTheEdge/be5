package com.developmentontheedge.be5.server.operations;

import com.developmentontheedge.be5.database.sql.parsers.ConcatColumnsParser;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.operation.OperationStatus;
import com.developmentontheedge.be5.server.model.FrontendAction;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import com.developmentontheedge.beans.json.JsonFactory;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.developmentontheedge.be5.server.FrontendActions.GO_BACK;
import static org.junit.Assert.assertEquals;

public class StandardOperationsDBTest extends ServerBe5ProjectDBTest
{
    private Long id;

    @Before
    public void addRecords()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);

        db.update("DELETE FROM testtableAdmin");
        id = db.insert("INSERT INTO testtableAdmin (name, valueCol) VALUES (?, ?)", "TestName", 1);
    }

    @Test
    public void insertOperation()
    {
        InsertOperation operation = (InsertOperation)createOperation("testtable", "All records", "Insert", "");
        OperationResult result = executeOperation(operation, doubleQuotes("{'name':'test2','valueCol':'2'}")).getSecond();
        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals("table/testtable/All records", ((FrontendAction[])result.getDetails())[0].getValue());

        Long lastInsertID = (Long)operation.getLastInsertID();
        RecordModel<Object> record = database.getEntity("testtable").get(lastInsertID);

        assertEquals("test2", record.getValueAsString("name"));
        assertEquals(2L, (long)record.getValueAsLong("valueCol"));
    }

    @Test
    public void editOperationGenerate()
    {
        Object first = generateOperation("testtableAdmin", "All records", "Edit", id.toString(), "{}").getFirst();

        assertEquals("{'name':'TestName','valueCol':'1'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editWithCheckRecords()
    {
        Object first = generateOperation("testtableAdmin", "All records",
                "EditWithCheckRecords", id.toString(), "{}").getFirst();

        assertEquals("{'name':'TestName','valueCol':'1'}",
                oneQuotes(JsonFactory.bean(first).getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        OperationResult result = executeOperation("testtableAdmin", "All records", "Edit", id.toString(),
                doubleQuotes("{'name':'EditName','valueCol':123}")).getSecond();

        assertEquals(OperationStatus.FINISHED, result.getStatus());
        assertEquals(GO_BACK, ((FrontendAction[])result.getDetails())[0].getType());
        assertEquals("table/testtableAdmin/All records/_selectedRows_=" + id, ((FrontendAction[])result.getDetails())[0].getValue());

        assertEquals("EditName,123",
                db.select("SELECT name, valueCol FROM testtableAdmin WHERE id = ?", new ConcatColumnsParser(), id));
    }

    @Test
    public void deleteInvoke()
    {
        database.getEntity("testGenCollection").add(ImmutableMap.of(
                "recordID", "testtableAdmin." + id.toString(),
                "categoryID", 123123L
        ));

        executeOperation("testtableAdmin", "All records", "Delete", id.toString(), "").getSecond();

        assertEquals(0, database.getEntity("testGenCollection").count(Collections.singletonMap("isDeleted___", "no")));
    }

}
