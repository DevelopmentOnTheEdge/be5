package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.query.QuerySession;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;


public class ServerTestQuerySession implements QuerySession
{
    private final ServerTestSession testSession;

    @Inject
    public ServerTestQuerySession(ServerTestSession testSession)
    {
        this.testSession = testSession;
    }

    @Override
    public Object get(String name)
    {
        return testSession.get(name);
    }
}
