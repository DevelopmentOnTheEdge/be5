package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.validation.rule.ValidationRules
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Entity
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import com.developmentontheedge.beans.BeanInfoConstants
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
        assertEquals "{'/CODE':{'displayName':'Код','columnSize':'2'},'/payable':{'displayName':'Оплачиваемая','canBeNull':true,'tagList':[['yes','да'],['no','нет']]},'/admlevel':{'displayName':'Административный уровень','tagList':[['Federal','Федеральный'],['Municipal','Муниципальный'],['Regional','Региональный']]},'/referenceTest':{'displayName':'Тест выборки','canBeNull':true,'columnSize':'2','tagList':[['01','Региональный'],['02','Муниципальный'],['03','Федеральный'],['04','Региональный']]}}",
            oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDynamicPropertyTest()
    {
        DynamicProperty property = dpsHelper.getDynamicPropertyWithoutTags(
                meta.getColumn(meta.getEntity("testTags"), "CODE"), meta.getEntity("testTags"))
        assertEquals "CODE", property.getName()
        assertEquals String.class, property.getType()
        assertEquals null, property.getValue()
    }

    @Test
    void getDynamicPropertyLocalizationTest()
    {
        DynamicProperty property = dpsHelper.getDynamicPropertyWithoutTags(
                meta.getColumn(meta.getEntity("testTags"), "admlevel"),
                meta.getEntity("testTags")
        )

        assertEquals "Административный уровень", property.getDisplayName()
    }

    @Test
    void getDynamicPropertyLocalizationForQueryTest()
    {
        DynamicProperty property = dpsHelper.getDynamicPropertyWithoutTags(
                meta.getColumn(meta.getEntity("testTags"), "admlevel"),
                meta.getEntity("testTags").getQueries().get("TestLocalizQuery")
        )

        assertEquals "Test Уровень", property.getDisplayName()
    }

    @Test
    void getDynamicPropertyLocalizationForOperationTest()
    {
        DynamicProperty property = dpsHelper.getDynamicPropertyWithoutTags(
                meta.getColumn(meta.getEntity("testTags"), "admlevel"),
                meta.getEntity("testTags").getOperations().get("Insert")
        )

        assertEquals "Уровень", property.getDisplayName()
    }

    @Test
    void getDpsForValuesTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals 2, dps.size()
        assertNotNull dps.getProperty("payable").getAttribute(BeanInfoConstants.TAG_LIST_ATTR)

        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        dps = new DynamicPropertySetSupport()
        list = dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["payable", "CODE"]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void getDpsForValuesTestOnlyForSpecifiedColumns()
    {
        dps.add(new DynamicProperty("payable", String.class))
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE"])

        assertNull dps.getProperty("payable").getAttribute(BeanInfoConstants.TAG_LIST_ATTR)

        dps = new DynamicPropertySetSupport()
        dps.add(new DynamicProperty("payable", String.class))
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("testTags"), ["payable"])

        assertNull dps.getProperty("payable").getAttribute(BeanInfoConstants.TAG_LIST_ATTR)
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
        assertArrayEquals([null, "TestName", null, null, null, null, null, "no"] as Object[], dpsHelper.getValues(dps))
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
                "(ID, name, value, whoInserted___, whoModified___, creationDate___, modificationDate___, isDeleted___) " +
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
    void getAsMapTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        assertEquals([CODE:null, payable:"yes", test: 2], dpsHelper.getAsMap(dps) << [test: 2])

        assertEquals([CODE:"12", payable:"yes"], dpsHelper.getAsMap(dps, [CODE:"12"]))
    }

    @Test
    void setOperationParamsTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"])

        dpsHelper.setOperationParams(dps, [payable:"no"])

        assertEquals([CODE:null, payable:"no"], dpsHelper.getAsMap(dps))

        assertTrue dps.getProperty("payable").getBooleanAttribute(BeanInfoConstants.READ_ONLY)
    }

}