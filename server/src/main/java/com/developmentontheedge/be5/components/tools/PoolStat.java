package com.developmentontheedge.be5.components.tools;

import com.developmentontheedge.be5.BoneCPDatabaseConnector;
import com.developmentontheedge.be5.DBCPDatabaseConnector;
import com.developmentontheedge.be5.metadata.sql.DatabaseConnector;
import com.developmentontheedge.be5.Utils;
import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;

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
        DatabaseConnector connector = Utils.getDefaultConnector();
        
        res.sendAsRawJson(new PoolStatResponse(getResult(connector)));
    }

    private String getResult(DatabaseConnector connector)
    {
        if (connector instanceof BoneCPDatabaseConnector)
        {
            return ((BoneCPDatabaseConnector) connector).getConnectionsStatistics();
        }
        else if (connector instanceof DBCPDatabaseConnector)
        {
            return ((DBCPDatabaseConnector) connector).getConnectionsStatistics();
        }
        
        return "This connector does not allow to view its statistics.";
    }
    
}
