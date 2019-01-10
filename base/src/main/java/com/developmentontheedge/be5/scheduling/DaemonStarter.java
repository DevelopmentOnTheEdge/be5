package com.developmentontheedge.be5.scheduling;

import com.developmentontheedge.be5.metadata.model.Daemon;
import org.quartz.JobDetail;


public interface DaemonStarter
{
    void reInitQuartzDaemon(Daemon daemon, boolean initManualDaemon);

    boolean isEnabled(String section);

    JobDetail getJobDetail(String jobName);

    boolean isJobRunning(String jobName);
}
