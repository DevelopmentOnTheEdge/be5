package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.test.AbstractProjectTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
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
    void getDpsWithoutAutoIncrementTest(){
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

    @Test
    void getDynamicPropertyTest()
    {
        DynamicProperty property = sqlHelper.getDynamicProperty(meta.getColumn(meta.getEntity("testTags"), "CODE"))
        assertEquals "CODE", property.getName()
        assertEquals String.class, property.getType()
        assertEquals null, property.getValue()
    }

    @Test
    void getDpsForValuesTest()
    {
        DynamicPropertySet dps = sqlHelper.getDpsForValues(meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        assertEquals String.class, dps.getProperty("CODE").getType()
        assertEquals String.class, dps.getProperty("payable").getType()
    }

    @Test
    void getValuesTest() throws Exception
    {
        DynamicPropertySet dps = sqlHelper.getDps(meta.getEntity("meters"))
        dps.setValue("name", "TestName")
        assertArrayEquals( [null,null,"TestName",null,null,null,null,null] as Object[], sqlHelper.getValues(dps))
    }

    @Test
    void generateInsertSqlTest() throws Exception
    {
        Entity metersEntity = meta.getEntity("meters")

        String sql = sqlHelper.generateInsertSql(metersEntity, sqlHelper.getDps(metersEntity))
        assertEquals "INSERT INTO meters " +
                "(whoModified___, whoInserted___, name, ID, modificationDate___, value, creationDate___, isDeleted___) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", sql
    }


}