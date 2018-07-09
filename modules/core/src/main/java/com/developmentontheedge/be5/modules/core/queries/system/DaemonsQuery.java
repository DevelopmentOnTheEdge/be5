package com.developmentontheedge.be5.modules.core.queries.system;

import com.developmentontheedge.be5.base.services.CoreUtils;
import com.developmentontheedge.be5.metadata.model.Daemon;
import com.developmentontheedge.be5.modules.core.services.scheduling.DaemonStarter;
import com.developmentontheedge.be5.query.model.TableModel;
import com.developmentontheedge.be5.server.queries.support.TableBuilderSupport;

import javax.inject.Inject;
import java.util.List;


public class DaemonsQuery extends TableBuilderSupport
{
    @Inject private CoreUtils coreUtils;
    @Inject private DaemonStarter daemonStarter;

    @Override
    public TableModel getTableModel()
    {
        List<Daemon> daemons = meta.getDaemons();

        addColumns("Name", "DaemonType", "Status", "ConfigSection", "Description", "ClassName");

        for (Daemon daemon : daemons)
        {
            addRow(daemon.getName(), cells(
                    daemon.getName(),
                    daemon.getDaemonType(),
                    Boolean.valueOf(daemonStarter.isJobRunning(daemon.getName())).toString(),
                    daemon.getConfigSection(),
                    daemon.getDescription(),
                    daemon.getClassName()
            ));
        }


        return table(columns, rows, true);
    }

}
