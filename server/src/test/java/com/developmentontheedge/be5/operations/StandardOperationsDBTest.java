package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.OperationDBTest;
import com.developmentontheedge.be5.test.TestUtils;
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
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Edit", id.toString(),"{}");

        FormPresentation first = operationService.generate(req).getFirst();

        assertEquals("{'name':'TestName','value':1}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void editInvoke()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Edit", id.toString(),
                "{'name':'EditName','value':123}");

        OperationResult operationResult = operationService.execute(req).getSecond();

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationResult);

        assertEquals("EditName,123",
                db.select("SELECT name, value FROM testtableAdmin WHERE id = ?", TestUtils::resultSetToString, id));
    }

}