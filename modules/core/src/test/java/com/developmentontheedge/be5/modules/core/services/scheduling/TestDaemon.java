package com.developmentontheedge.be5.modules.core.services.scheduling;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.modules.core.model.scheduling.Process;
import org.quartz.JobExecutionContext;

import javax.inject.Inject;


public class TestDaemon extends Process
{
    private final DbService db;

    @Inject
    public TestDaemon(DbService db)
    {
        this.db = db;
    }

    @Override
    public void doWork(JobExecutionContext context) throws Exception
    {

    }
}
