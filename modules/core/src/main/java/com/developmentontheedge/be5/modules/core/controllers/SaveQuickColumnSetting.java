package com.developmentontheedge.be5.modules.core.controllers;

import com.developmentontheedge.be5.base.model.UserInfo;
import com.developmentontheedge.be5.base.services.UserInfoProvider;
import com.developmentontheedge.be5.databasemodel.DatabaseModel;
import com.developmentontheedge.be5.databasemodel.RecordModel;
import com.developmentontheedge.be5.server.servlet.support.JsonApiController;
import com.developmentontheedge.be5.web.Request;
import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;

@Singleton
public class SaveQuickColumnSetting extends JsonApiController
{
    private final DatabaseModel database;
    private final UserInfoProvider userInfoProvider;

    @Inject
    public SaveQuickColumnSetting(DatabaseModel database, UserInfoProvider userInfoProvider)
    {
        this.database = database;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public Object generate(Request req, String requestSubUrl)
    {
        UserInfo userInfo = userInfoProvider.get();
        String table_name = req.get("table_name");
        String query_name = req.get("query_name");
        String column_name = req.get("column_name");
        String quick = req.get("quick");

        String ret;
        RecordModel<Object> settings = database.getEntity("columnSettings").getBy(ImmutableMap.of(
                "table_name", table_name,
                "query_name", query_name,
                "column_name", column_name,
                "user_name", userInfo.getUserName()
        ));
        if (settings != null)
        {
            ret = "" + settings.update(ImmutableMap.of("quick", quick));
        }
        else
        {
            ret = "" + database.getEntity("columnSettings").add(new HashMap<String, Object>() {{
                put("queryID", 0);
                put("table_name", table_name);
                put("query_name", query_name);
                put("column_name", column_name);
                put("user_name", userInfo.getUserName());
                put("quick", quick);
            }});
        }

        return ret;
    }
}
