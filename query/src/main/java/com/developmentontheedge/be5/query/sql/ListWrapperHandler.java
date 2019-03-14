package com.developmentontheedge.be5.query.sql;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ListWrapperHandler<T> implements ResultSetHandler<List<T>>
{
    private final ResultSetParser<T> parser;

    public ListWrapperHandler(ResultSetParser<T> parser)
    {
        this.parser = parser;
    }

    @Override
    public List<T> handle(ResultSet rs) throws SQLException
    {
        List<T> rows = new ArrayList<>();
        while (rs.next())
        {
            rows.add(parser.parse(rs));
        }
        return rows;
    }
}
