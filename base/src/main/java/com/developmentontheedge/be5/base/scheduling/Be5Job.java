package com.developmentontheedge.be5.base.scheduling;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.security.UserInfoHolder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.developmentontheedge.be5.metadata.RoleType.ROLE_ADMINISTRATOR;
import static java.util.Collections.singletonList;


public abstract class Be5Job implements Job
{
    private static final Logger log = Logger.getLogger(Be5Job.class.getName());

    private static final UserInfo JobAdmin = new UserInfo("JobAdmin",
            singletonList(ROLE_ADMINISTRATOR), singletonList(ROLE_ADMINISTRATOR));

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException
    {
        UserInfoHolder.setLoggedUser(JobAdmin);
        try
        {
            doWork(context);
        }
        catch (Throwable e)
        {
            log.log(Level.SEVERE, "Error in job: ", e);
            throw new JobExecutionException(e);
        }
        UserInfoHolder.setLoggedUser(null);
    }

    public abstract void doWork(JobExecutionContext context) throws Exception;
}
