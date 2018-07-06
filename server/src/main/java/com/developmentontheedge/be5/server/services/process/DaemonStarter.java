package com.developmentontheedge.be5.server.services.process;

import com.developmentontheedge.be5.metadata.model.Daemon;

public interface DaemonStarter
{
    void shutdown();

    void reInitQuartzDaemon(Daemon daemon, boolean initManualDaemon);

    boolean isEnabled(String section);
}
