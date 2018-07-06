package com.developmentontheedge.be5.test.mocks;

import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.server.services.process.DaemonStarter;


public class DaemonStarterForTest implements DaemonStarter
{
    @Override
    public void shutdown()
    {

    }

    @Override
    public void reInitQuartzDaemon(Daemon daemon, boolean initManualDaemon)
    {

    }

    @Override
    public boolean isEnabled(String section)
    {
        return false;
    }
}
