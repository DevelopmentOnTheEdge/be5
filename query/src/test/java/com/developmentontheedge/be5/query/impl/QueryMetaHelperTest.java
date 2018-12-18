package com.developmentontheedge.be5.query.impl;

import com.developmentontheedge.be5.query.QueryBe5ProjectDBTest;
import com.developmentontheedge.sql.model.AstStart;
import com.developmentontheedge.sql.model.SqlQuery;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;

public class QueryMetaHelperTest extends QueryBe5ProjectDBTest
{
    @Inject private QueryMetaHelper queryMetaHelper;

    @Test
    public void resolveTypeOfRefColumn()
    {
        queryMetaHelper.resolveTypeOfRefColumn(SqlQuery.parse("select * from filterTestTable t " +
              "WHERE CONCAT(t.firstName, ' ', t.lastName) LIKE CONCAT('%<parameter:queryString/>%')"));
    }

    @Test
    public void notResolveForColumnInAnyFunction()
    {
        AstStart sql = SqlQuery.parse("select * from classifications " +
                "WHERE FORMAT_DATE(creationDate___) = '<parameter:creationDate___ />'");
        queryMetaHelper.resolveTypeOfRefColumn(sql);
        assertEquals("SELECT * FROM classifications " +
                "WHERE FORMAT_DATE(creationDate___) = '<parameter:creationDate___ />'", sql.format());
    }
}
