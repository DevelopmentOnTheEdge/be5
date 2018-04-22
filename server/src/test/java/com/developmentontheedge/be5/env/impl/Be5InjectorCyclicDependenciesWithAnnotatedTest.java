package com.developmentontheedge.be5.env.impl;

import com.developmentontheedge.be5.util.Be5;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.env.Injector;
import com.developmentontheedge.be5.env.Stage;
import com.developmentontheedge.be5.env.impl.testServices.AService;
import com.developmentontheedge.be5.env.impl.testServices.BService;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import com.developmentontheedge.be5.test.mocks.SqlServiceMock;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.verify;


public class Be5InjectorCyclicDependenciesWithAnnotatedTest extends Be5ProjectTest
{
    @Inject private Injector injector;

    @Before
    public void before(){
        SqlServiceMock.clearMock();
    }

    @Test
    public void injectWithAnnotatedAServiceFirst()
    {
        injector.get(AService.class).aMethodUseBService();
        verify(SqlServiceMock.mock).update("bMethod sql");

        injector.get(BService.class).bMethodUseAService();
        verify(SqlServiceMock.mock).update("aMethod sql");
    }

    @Test
    public void injectWithAnnotatedBServiceFirst()
    {
        Injector sqlMockInjector2 = Be5.createInjector(Stage.DEVELOPMENT, new SqlMockBinder());

        sqlMockInjector2.get(BService.class).bMethodUseAService();
        verify(SqlServiceMock.mock).update("aMethod sql");

        sqlMockInjector2.get(AService.class).aMethodUseBService();
        verify(SqlServiceMock.mock).update("bMethod sql");
    }

}