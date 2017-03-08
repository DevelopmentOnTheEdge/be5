package com.developmentontheedge.be5.components.tools;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.dbms.DbmsConnector;

public class PoolStat implements Component {
    
    static class PoolStatResponse
    {
        final String result;

        PoolStatResponse(String result)
        {
            this.result = result;
        }
    }

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        DbmsConnector connector = null; //TODO Utils.getDefaultConnector();

        res.sendAsRawJson(new PoolStatResponse(getResult(connector)));
    }

    private String getResult(DbmsConnector connector)
    {
//TODO        if (connector instanceof BoneCPDatabaseConnector)
//        {
//            return ((BoneCPDatabaseConnector) connector).getConnectionsStatistics();
//        }
//        else if (connector instanceof DBCPDatabaseConnector)
//        {
//            return ((DBCPDatabaseConnector) connector).getConnectionsStatistics();
//        }

        return "This connector does not allow to view its statistics.";
    }
    
}
