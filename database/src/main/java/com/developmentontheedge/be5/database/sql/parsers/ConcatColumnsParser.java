package com.developmentontheedge.be5.database.sql.parsers;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.ResultSetWrapper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ConcatColumnsParser implements ResultSetParser<String>
{
    @Override
    public String parse(ResultSetWrapper rs) throws SQLException
    {
        List<String> list = new ArrayList<>();
        try
        {
            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
            {
                if (rs.getObject(i) != null)
                    list.add(rs.getObject(i).toString());
                else
                {
                    list.add("null");
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return list.stream().collect(Collectors.joining(","));
    }
}
