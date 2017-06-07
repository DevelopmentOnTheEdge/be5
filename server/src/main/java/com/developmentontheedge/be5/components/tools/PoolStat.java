package com.developmentontheedge.be5.components.tools;

import com.developmentontheedge.be5.api.Component;
import com.developmentontheedge.be5.api.Request;
import com.developmentontheedge.be5.api.Response;
import com.developmentontheedge.be5.api.ServiceProvider;
import com.developmentontheedge.be5.api.services.DatabaseService;


public class PoolStat implements Component {

    public static class PoolStatResponse
    {
        final String result;

        PoolStatResponse(String result)
        {
            this.result = result;
        }

        public String getResult()
        {
            return result;
        }
    }

    @Override
    public void generate(Request req, Response res, ServiceProvider serviceProvider)
    {
        DatabaseService dbs = serviceProvider.getDatabaseService();

        res.sendAsRawJson(new PoolStatResponse(dbs.getConnectionsStatistics()));
    }

    
}
