package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.beans.QRec;


public interface QRecService
{
    QRec of(String sql, Object... params);
}
