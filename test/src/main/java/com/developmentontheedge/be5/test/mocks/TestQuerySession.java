package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.query.QuerySession;

import javax.inject.Inject;


public class TestQuerySession implements QuerySession
{
    private final TestSession testSession;

    @Inject
    public TestQuerySession(TestSession testSession)
    {
        this.testSession = testSession;
    }

    @Override
    public Object get(String name)
    {
        return testSession.get(name);
    }
}
