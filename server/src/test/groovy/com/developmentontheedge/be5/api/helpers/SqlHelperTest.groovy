package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.test.AbstractProjectTest
import org.junit.Test

import static org.junit.Assert.*

class SqlHelperTest extends AbstractProjectTest
{
    SqlHelper sqlHelper = injector.get(SqlHelper.class)

    @Test
    void inClause() throws Exception
    {
        assertEquals "(?, ?, ?, ?, ?)", sqlHelper.inClause(5)

        assertEquals "SELECT code FROM table WHERE id IN (?, ?, ?, ?, ?)",
                "SELECT code FROM table WHERE id IN " + sqlHelper.inClause(5)
    }

}