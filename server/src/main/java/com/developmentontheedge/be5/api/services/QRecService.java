package com.developmentontheedge.be5.api.services;

import com.developmentontheedge.be5.model.QRec;


/**
 * Reads the first record of thsq SQL query as Dynamic Property Set
 *
 * <br><br>To get record values, you can use:
 * <br/>{@link QRec#getString(String) getString(String)}
 * <br/>{@link QRec#getInt(String) getInt(String)}
 * <br/>{@link QRec#getLong(String) getLong(String)}.
 *
 */
public interface QRecService
{
    QRec of(String sql, Object... params);

    QRec withCache(String sql, Object... params);
}
