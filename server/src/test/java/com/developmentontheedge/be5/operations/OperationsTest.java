package com.developmentontheedge.be5.operations;

import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.services.OperationService;
import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.model.FormPresentation;
import com.developmentontheedge.be5.operation.OperationResult;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class OperationsTest extends AbstractProjectTest{

    private Injector sqlMockInjector = Be5.createInjector(new SqlMockBinder());
    private OperationService operationService = sqlMockInjector.get(OperationService.class);

    @BeforeClass
    public static void beforeClass(){
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR, RoleType.ROLE_SYSTEM_DEVELOPER);
    }

    @AfterClass
    public static void afterClass(){
        initUserWithRoles(RoleType.ROLE_GUEST);
    }

    @Test
    public void deleteOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Delete", "1", "");

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records"),
                operationService.generate(req).getSecond());

        verify(SqlServiceMock.mock).update("delete from testtableAdmin where id = ?", 1L);
    }

    @Test
    public void insertOperationInitValues()
    {
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "1","{}");

        FormPresentation first = operationService.generate(req).getFirst();
        assertEquals("{'name':'','value':0}",
                oneQuotes(first.getBean().getJsonObject("values").toString()));
    }

    @Test
    public void insertOperation(){
        Request req = getSpyMockRecForOp("testtableAdmin", "All records", "Insert", "1",
                "{'name':'test','value':1}");

        FormPresentation first = operationService.generate(req).getFirst();
        assertEquals("{" +
                        "'values':{'name':'test','value':1}," +
                        "'meta':{" +
                            "'/name':{'displayName':'name'}," +
                            "'/value':{'displayName':'value','type':'Integer'}}," +
                        "'order':['/name','/value']}",
                oneQuotes(first.getBean().toString()));

        //OperationResult execute = operationService.execute(req);

        assertEquals(OperationResult.redirect("table/testtableAdmin/All records/name=test/value=1"),
                operationService.execute(req).getSecond());

        verify(SqlServiceMock.mock).insert("INSERT INTO testtableAdmin (name, value) " +
                "VALUES (?, ?)", "test", 1);
    }

}