package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*


class SqlHelperTest extends AbstractProjectTest
{
    SqlHelper sqlHelper = injector.get(SqlHelper.class)
    Meta meta = injector.get(Meta.class)

    @Test
    void inClause() throws Exception
    {
        assertEquals "(?, ?, ?, ?, ?)", sqlHelper.inClause(5)

        assertEquals "SELECT code FROM table WHERE id IN (?, ?, ?, ?, ?)",
                "SELECT code FROM table WHERE id IN " + sqlHelper.inClause(5)
    }

    @Test
    void getDpsWithoutAutoIncrement(){
        def dps = sqlHelper.getDpsWithoutAutoIncrement(meta.getEntity("testTags"))
        assertEquals "{" +
            "'/referenceTest':{'displayName':'Тест выборки','canBeNull':true," +
                "'tagList':[['01','Региональный'],['02','Муниципальный'],['03','Федеральный'],['04','Региональный']]}," +
            "'/CODE':{'displayName':'Код'}," +
            "'/payable':{'displayName':'Оплачиваемая'," +
                "'tagList':[['yes','да'],['no','нет']]}," +
            "'/admlevel':{'displayName':'Уроверь'," +
                "'tagList':[['Federal','Федеральный'],['Municipal','Муниципальный'],['Regional','Региональный']]}" +
        "}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

}