package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.base.scheduling.DaemonStarter;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.query.model.beans.QRec;
import com.developmentontheedge.be5.server.queries.support.QueryExecutorSupport;

import javax.inject.Inject;
import java.util.List;


public class DaemonsTable extends QueryExecutorSupport
{
    @Inject private DaemonStarter daemonStarter;

    @Override
    public List<QRec> execute()
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
