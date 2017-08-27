package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.env.Be5;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.impl.testServices.AService;
import com.developmentontheedge.be5.env.impl.testServices.BService;
import com.developmentontheedge.be5.test.AbstractProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;


public class Be5InjectorCyclicDependenciesWithAnnotatedTest extends AbstractProjectTest
{
    @Before
    public void before(){
        SqlServiceMock.clearMock();
    }

    @Test
    public void injectWithAnnotatedAServiceFirst() throws Exception
    {
        sqlMockInjector.get(AService.class).aMethodUseBService();
        verify(SqlServiceMock.mock).update("bMethod sql");

        sqlMockInjector.get(BService.class).bMethodUseAService();
        verify(SqlServiceMock.mock).update("aMethod sql");
    }

    @Test
    public void injectWithAnnotatedBServiceFirst() throws Exception
    {
        Injector sqlMockInjector2 = Be5.createInjector(new SqlMockBinder());

        sqlMockInjector2.get(BService.class).bMethodUseAService();
        verify(SqlServiceMock.mock).update("aMethod sql");

        sqlMockInjector2.get(AService.class).aMethodUseBService();
        verify(SqlServiceMock.mock).update("bMethod sql");
    }

}