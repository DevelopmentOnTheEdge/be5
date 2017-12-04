package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*


class DpsHelperTest extends Be5ProjectDBTest
{
    @Inject DpsHelper dpsHelper
    @Inject Meta meta

    DynamicPropertySet dps

    @Before
    void before()
    {
        dps = new DynamicPropertySetSupport()
    }

    @Test
    void getDpsWithoutAutoIncrementTest()
    {
        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("testTags"))
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
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        dps = new DynamicPropertySetSupport()
        list = dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void addDynamicPropertiesTest()
    {
        dpsHelper.addDynamicProperties(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        dps = new DynamicPropertySetSupport()
        list = dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void getDpsForColumnsTestWithValues()
    {
        def presetValues = [notContainColumn: "2", testLong: "3", payable: "no"]
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"], presetValues)

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
        dpsHelper.addDpForColumns(dps, meta.getEntity("meters"), ["name"], presetValues)

    }

    @Test
    void getValuesTest() throws Exception
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"))
        dps.setValue("name", "TestName")
        assertArrayEquals([null, null, "TestName", null, null, null, null, "no"] as Object[], dpsHelper.getValues(dps))
    }

    @Test
    void getDpsTest() throws Exception
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"))
        assertNotNull dps.getProperty("value")

        dps = new DynamicPropertySetSupport()
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("meters"), Collections.singletonList("value"))
        assertNull dps.getProperty("value")
    }

    @Test
    void generateInsertSqlTest() throws Exception
    {
        Entity metersEntity = meta.getEntity("meters")

        String sql = dpsHelper.generateInsertSql(metersEntity, dpsHelper.addDp(dps, metersEntity))
        assertEquals "INSERT INTO meters " +
                "(whoModified___, whoInserted___, name, ID, modificationDate___, value, creationDate___, isDeleted___) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)", sql
    }

    @Test
    void getLabelAndGetLabelRawTest() throws Exception
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addLabel(dps, "test")
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dpsHelper.addLabelRaw(dps, "test")
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','rawValue':true,'labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dpsHelper.addLabel(dps, "customName", "test")
        assertEquals "{'/customName':{'displayName':'customName','labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
        dps.remove("customName")

        dpsHelper.addLabelRaw(dps, "customName", "test")
        assertEquals "{'/customName':{'displayName':'customName','rawValue':true,'labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDpsWithLabelANDNotSubmittedTest()
    {
        def dps = dpsHelper.addDpWithLabelANDNotSubmitted(dps, "test")
        assertEquals "{'/infoLabel':{'displayName':'infoLabel','labelField':true},'/notSubmitted':{'displayName':'notSubmitted','hidden':true}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDpsWithLabelRawANDNotSubmittedTest()
    {
        def dps = dpsHelper.addDpWithLabelRawANDNotSubmitted(dps, "test")
        assertEquals "{'/infoLabel':{'displayName':'infoLabel','rawValue':true,'labelField':true},'/notSubmitted':{'displayName':'notSubmitted','hidden':true}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getAsMapTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals([CODE:null, payable:"yes", test: 2], dpsHelper.getAsMap(dps) << [test: 2])

        assertEquals([CODE:"12", payable:"yes"], dpsHelper.getAsMap(dps, [CODE:"12"]))
    }

}