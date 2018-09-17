package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.modules.core.services.scheduling.DaemonStarter;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;

import javax.inject.Inject;


public class DaemonsTable extends TableBuilderSupport
{
    @Inject private DaemonStarter daemonStarter;

    @Override
    public TableModel getTableModel()
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
        return table(columns, rows, true);
    }

}
