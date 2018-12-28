package com.developmentontheedge.be5.query;

import com.developmentontheedge.be5.query.model.beans.QRec;

import java.util.List;
import java.util.Map;

public interface QueryExecutor
{
    Map<String, Object> getParameters();

    int getOrderColumn();

    String getOrderDir();

    int getOffset();

    int getLimit();

    @Deprecated//TODO move logic to frontend
    Boolean isSelectable();

    /**
     * Executes the query.
     */
    List<QRec> execute();

    /**
     * Counts the number of resulting rows.
     */
    long count();
}
