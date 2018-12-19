package com.developmentontheedge.be5.query.sql;

import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TagsMapHandler implements ResultSetHandler<Map<String, String>>
{
    @Override
    public Map<String, String> handle(ResultSet rs) throws SQLException
    {
        Map<String, String> tags = new LinkedHashMap<>();
        while (rs.next())
        {
            tags.put(rs.getObject(1).toString(), rs.getObject(2).toString());
        }
        return tags;
    }
}
