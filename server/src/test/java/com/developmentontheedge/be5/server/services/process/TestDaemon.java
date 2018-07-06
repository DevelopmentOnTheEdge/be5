package com.developmentontheedge.be5.server.services.process;

import com.developmentontheedge.be5.database.DbService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;


public class TestDaemon implements Job
{
    private final DbService db;

    @Inject
    public TestDaemon(DbService db)
    {
        this.db = db;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {

    }
}
