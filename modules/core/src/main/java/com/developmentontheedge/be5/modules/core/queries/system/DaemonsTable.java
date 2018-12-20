package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.modules.core.services.scheduling.DaemonStarter;
import com.developmentontheedge.be5.server.queries.support.DpsTableBuilderSupport;
import com.developmentontheedge.beans.DynamicPropertySet;

import javax.inject.Inject;
import java.util.List;


public class DaemonsTable extends DpsTableBuilderSupport
{
    @Inject private DaemonStarter daemonStarter;

    @Override
    public List<DynamicPropertySet> getTableModel()
    {
        addColumns("Name", "Type", "Status", "Running", "Meta");

        for (Daemon daemon : meta.getDaemons())
        {
            addRow(daemon.getName(), cells(
                    daemon.getName(),
                    daemon.getDaemonType(),
                    daemonStarter.isEnabled(daemon.getName()),
                    Boolean.valueOf(daemonStarter.isJobRunning(daemon.getName())).toString(),
                    "ConfigSection: " + daemon.getConfigSection() + ",<br> " +
                    "Description: " + daemon.getDescription() + ",<br> " +
                    "ClassName: " + daemon.getClassName()
            ));
        }
        return table(true);
    }

}
