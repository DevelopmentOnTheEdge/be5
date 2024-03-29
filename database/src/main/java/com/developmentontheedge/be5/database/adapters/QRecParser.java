package com.developmentontheedge.be5.database.adapters;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.QRec;

import java.sql.ResultSet;
import java.sql.SQLException;

public class QRecParser implements ResultSetParser<QRec>
{
    @Override
    public QRec parse(ResultSet rs) throws SQLException
    {
        return DpsRecordAdapter.addDp(new QRec(), rs);
    }
}
