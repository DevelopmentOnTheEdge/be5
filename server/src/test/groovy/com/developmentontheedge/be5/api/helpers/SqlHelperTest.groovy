package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*


class SqlHelperTest extends Be5ProjectDBTest
{
    @Inject private SqlHelper sqlHelper
    @Inject private Meta meta

    @Test
    void getDpsWithoutAutoIncrementTest(){
        def dps = sqlHelper.getDpsWithoutAutoIncrement(meta.getEntity("testTags"))
        assertEquals "{" +
            "'/referenceTest':{'displayName':'Тест выборки','canBeNull':true," +
                "'tagList':[['01','Региональный'],['02','Муниципальный'],['03','Федеральный'],['04','Региональный']]}," +
            "'/CODE':{'displayName':'Код'}," +
            "'/payable':{'displayName':'Оплачиваемая','canBeNull':true," +
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
        DynamicPropertySet dps = sqlHelper.getDpsForColumns(meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        list = sqlHelper.getDpsForColumns(meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void getValuesTest() throws Exception
    {
        DynamicPropertySet dps = sqlHelper.getDps(meta.getEntity("meters"))
        dps.setValue("name", "TestName")
        assertArrayEquals( [null,null,"TestName",null,null,null,null,null] as Object[], sqlHelper.getValues(dps))
    }

    @Test
    void getDpsTest() throws Exception
    {
        DynamicPropertySet dps = sqlHelper.getDps(meta.getEntity("meters"))
        assertNotNull dps.getProperty("value")

        dps = sqlHelper.getDpsWithoutColumns(meta.getEntity("meters"), Collections.singletonList("value"))
        assertNull dps.getProperty("value")
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