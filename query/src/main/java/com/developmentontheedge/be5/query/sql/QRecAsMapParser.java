package com.developmentontheedge.be5.query.sql;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.QRec;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public class QRecAsMapParser implements ResultSetParser<Map<String, Object>>
{
    @Override
    public Map<String, Object> parse(ResultSet rs) throws SQLException
    {
        return DpsRecordAdapter.addDp(new QRec(), rs).asMap();
    }
}
