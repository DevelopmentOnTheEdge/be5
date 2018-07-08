package com.developmentontheedge.be5.modules.core.services.impl.scheduling;

import com.developmentontheedge.be5.metadata.model.Daemon;


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
