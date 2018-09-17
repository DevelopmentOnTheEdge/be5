package com.developmentontheedge.be5.query.sql;

import com.developmentontheedge.be5.database.sql.ResultSetParser;
import com.developmentontheedge.be5.database.sql.ResultSetWrapper;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;
import com.developmentontheedge.beans.DynamicPropertySetSupport;

import java.sql.SQLException;

public class DynamicPropertySetSimpleStringParser implements ResultSetParser<DynamicPropertySet>
{
    @Override
    public DynamicPropertySet parse(ResultSetWrapper rs) throws SQLException
    {
        DynamicProperty[] schema = DpsRecordAdapter.createSimpleStringSchema(rs.getMetaData());
        DynamicPropertySet dps = new DynamicPropertySetSupport();
        return DpsRecordAdapter.addDp(dps, schema, rs);
    }
}
