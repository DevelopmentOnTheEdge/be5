package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.query.QuerySession;
import com.developmentontheedge.be5.web.Session;

import javax.inject.Inject;


public class TestQuerySession implements QuerySession
{
    private final Session testSession;

    @Inject
    public TestQuerySession(Session session)
    {
        this.testSession = session;
    }

    @Override
    public Object get(String name)
    {
        return testSession.get(name);
    }

    @Override
    public void set(String name, Object value)
    {
        testSession.set(name, value);
    }
}
