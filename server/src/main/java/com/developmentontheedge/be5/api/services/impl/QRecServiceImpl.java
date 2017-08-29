package com.developmentontheedge.be5.api.services.impl;

import com.developmentontheedge.be5.api.helpers.DpsHelper;
import com.developmentontheedge.be5.api.services.QRecService;
import com.developmentontheedge.be5.api.services.SqlService;
import com.developmentontheedge.be5.beans.QRec;
import com.developmentontheedge.beans.DynamicProperty;
import com.developmentontheedge.beans.DynamicPropertySet;

public class QRecServiceImpl implements QRecService
{
    private SqlService db;

    public QRecServiceImpl(SqlService db)
    {
        this.db = db;
    }

    @Override
    public QRec select(String sql, Object... params)
    {
        DynamicPropertySet dps = db.select(sql, DpsHelper::createDps, params);

        if(dps == null)
        {
            return null;
        }
        else
        {
            QRec qRec = new QRec();
            for (DynamicProperty property : dps)
            {
                qRec.add(property);
            }
            return qRec;
        }
    }
}
