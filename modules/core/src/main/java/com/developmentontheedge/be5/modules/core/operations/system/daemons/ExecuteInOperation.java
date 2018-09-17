package com.developmentontheedge.be5.modules.core.operations.system.daemons;

import com.developmentontheedge.be5.operation.services.validation.Validation;
import com.developmentontheedge.be5.server.operations.support.OperationSupport;
import com.developmentontheedge.beans.BeanInfoConstants;
import com.developmentontheedge.beans.DynamicPropertySetSupport;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.quartz.Job;

import java.util.Map;

public class ExecuteInOperation extends OperationSupport
{
    @Inject
    private Injector guice;

    @Override
    public Object getParameters(Map<String, Object> presetValues) throws Exception
    {
        DynamicPropertySetSupport params = new DynamicPropertySetSupport();
        dpsHelper.addLabel(params, "Run in current thread!");
        params.getAsBuilder("infoLabel")
                .title("")
                .attr(BeanInfoConstants.STATUS, Validation.Status.WARNING);

        return params;
    }

    @Override
    public void invoke(Object parameters) throws Exception
    {
        String className = meta.getDaemons().stream()
                .filter(x -> x.getName().equals(context.getRecord()))
                .findFirst().get()
                .getClassName();
        Class<?> aClass = Class.forName(className);
        Job job = (Job) guice.getInstance(aClass);
        job.execute(null);
    }
}
