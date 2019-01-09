package com.developmentontheedge.be5.modules.core.services.scheduling;

import com.developmentontheedge.be5.database.DbService;
import com.developmentontheedge.be5.base.scheduling.Be5Job;
import org.quartz.JobExecutionContext;

import javax.inject.Inject;


public class TestDaemon extends Be5Job
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
        db.insert("INSERT INTO users(user_name,user_pass) VALUES (?, ?)",
                "TestDaemonUser" + System.currentTimeMillis(), "test");
    }
}
