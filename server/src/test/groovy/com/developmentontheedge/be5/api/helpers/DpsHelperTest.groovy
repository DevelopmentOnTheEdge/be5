package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Test

import static org.junit.Assert.*


class DpsHelperTest extends Be5ProjectDBTest
{
    @Inject private DpsHelper dpsHelper
    @Inject private Meta meta

    @Test
    void getDpsWithoutAutoIncrementTest(){
        def dps = dpsHelper.getDpsWithoutAutoIncrement(meta.getEntity("testTags"))
        assertEquals "{" +
            "'/referenceTest':{'displayName':'Тест выборки','canBeNull':true," +
                "'tagList':[['01','Региональный'],['02','Муниципальный'],['03','Федеральный'],['04','Региональный']]}," +
            "'/CODE':{'displayName':'Код'}," +
            "'/payable':{'displayName':'Оплачиваемая','canBeNull':true," +
                "'tagList':[['yes','да'],['no','нет']]}," +
            "'/admlevel':{'displayName':'Уроверь'," +
                "'tagList':[['Federal','Федеральный'],['Municipal','Муниципальный'],['Regional','Региональный']]}," +
            "'/testLong':{'displayName':'testLong','type':'Long','canBeNull':true}" +
        "}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDynamicPropertyTest()
    {
        DynamicProperty property = dpsHelper.getDynamicPropertyWithoutTags(meta.getColumn(meta.getEntity("testTags"), "CODE"))
        assertEquals "CODE", property.getName()
        assertEquals String.class, property.getType()
        assertEquals null, property.getValue()
    }

    @Test
    void getDpsForValuesTest()
    {
        DynamicPropertySet dps = dpsHelper.getDpsForColumns(meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        list = dpsHelper.getDpsForColumns(meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void addDynamicPropertiesTest()
    {
        DynamicPropertySet dps = new DynamicPropertySetSupport()

        dpsHelper.addDynamicProperties(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        list = dpsHelper.getDpsForColumns(meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void getDpsForColumnsTestWithValues()
    {
        def presetValues = [notContainColumn: "2", testLong: "3", payable: "no"]
        DynamicPropertySet dps = dpsHelper.getDpsForColumns(meta.getEntity("testTags"), ["CODE", "payable"], presetValues)

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()

        assertEquals "payable", list.get(1).getName()
        assertEquals "no", list.get(1).getValue()

//        assertEquals "notContainColumn", list.get(2).getName()
//        assertEquals String, dps.getProperty("notContainColumn").getType()
//        assertEquals "2", dps.getProperty("notContainColumn").getValue()

//        assertEquals "testLong", list.get(2).getName()
//        assertEquals Long, dps.getProperty("testLong").getType()
//        assertEquals "3", dps.getProperty("testLong").getValue()
    }

    @Test
    void getDpsForColumnsTestSpecial()
    {
        def presetValues = [notContainColumn: "2", testLong: "3", payable: "no"]
        DynamicPropertySet dps = dpsHelper.getDpsForColumns(meta.getEntity("meters"), ["name"], presetValues)

    }

    @Test
    void getValuesTest() throws Exception
    {
        DynamicPropertySet dps = dpsHelper.getDps(meta.getEntity("meters"))
        dps.setValue("name", "TestName")
        assertArrayEquals( [null,null,"TestName",null,null,null,null,"no"] as Object[], dpsHelper.getValues(dps))
    }

    @Test
    void getDpsTest() throws Exception
    {
        DynamicPropertySet dps = dpsHelper.getDps(meta.getEntity("meters"))
        assertNotNull dps.getProperty("value")

        dps = dpsHelper.getDpsWithoutColumns(meta.getEntity("meters"), Collections.singletonList("value"))
        assertNull dps.getProperty("value")
    }

    @Test
    void generateInsertSqlTest() throws Exception
    {
        Entity metersEntity = meta.getEntity("meters")

        String sql = dpsHelper.generateInsertSql(metersEntity, dpsHelper.getDps(metersEntity))
        assertEquals "INSERT INTO meters " +
                "(whoModified___, whoInserted___, name, ID, modificationDate___, value, creationDate___, isDeleted___) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", sql
    }

    @Test
    void getLabelAndGetLabelRawTest() throws Exception
    {
        def dps = new DynamicPropertySetSupport()
        dps.add(dpsHelper.getLabel("test"))
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dps.add(dpsHelper.getLabelRaw("test"))
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','rawValue':true,'labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dps.add(dpsHelper.getLabel("test", "customName"))
        assertEquals "{'/customName':{'displayName':'customName','labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
        dps.remove("customName")

        dps.add(dpsHelper.getLabelRaw("test", "customName"))
        assertEquals "{'/customName':{'displayName':'customName','rawValue':true,'labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDpsWithLabelANDNotSubmittedTest()
    {
        def dps = dpsHelper.getDpsWithLabelANDNotSubmitted("test")
        assertEquals "{'/infoLabel':{'displayName':'infoLabel','labelField':true},'/notSubmitted':{'displayName':'notSubmitted','hidden':true}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDpsWithLabelRawANDNotSubmittedTest()
    {
        def dps = dpsHelper.getDpsWithLabelRawANDNotSubmitted("test")
        assertEquals "{'/infoLabel':{'displayName':'infoLabel','rawValue':true,'labelField':true},'/notSubmitted':{'displayName':'notSubmitted','hidden':true}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }
}