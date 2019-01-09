package com.developmentontheedge.be5.base.scheduling;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class Be5Job implements Job
{
    private static final Logger log = Logger.getLogger(Be5Job.class.getName());

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            doWork(context);
            //updateLastExecutionResult( ok );
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in job: ", e);
            throw new JobExecutionException(e);
        }
    }

    public abstract void doWork(JobExecutionContext context) throws Exception;
}
