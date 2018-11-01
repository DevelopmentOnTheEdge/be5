package com.developmentontheedge.be5.server.helpers

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.database.sql.parsers.ConcatColumnsParser
import com.developmentontheedge.be5.query.sql.DynamicPropertySetSimpleStringParser
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest
import com.developmentontheedge.beans.BeanInfoConstants
import com.developmentontheedge.beans.DynamicProperty
import com.developmentontheedge.beans.DynamicPropertySet
import com.developmentontheedge.beans.DynamicPropertySetSupport
import com.developmentontheedge.beans.json.JsonFactory
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static com.developmentontheedge.beans.BeanInfoConstants.TAG_LIST_ATTR
import static org.junit.Assert.*

class DpsHelperTest extends ServerBe5ProjectDBTest
{
    @Inject
    DpsHelper dpsHelper
    @Inject
    Meta meta

    DynamicPropertySet dps

    @Before
    void setUpTestUtils()
    {
        initGuest()
    }

    @Before
    void before()
    {
        dps = new DynamicPropertySetSupport()
    }

    @Test
    void getDpsWithoutAutoIncrementTest()
    {
        dpsHelper.addDpExcludeAutoIncrement(dps, meta.getEntity("testTags"), [:])
        assertEquals "{'/CODE':{'displayName':'Код','columnSize':'2'},'/payable':{'displayName':'Оплачиваемая','canBeNull':true,'tagList':[['yes','да'],['no','нет']]},'/admlevel':{'displayName':'Административный уровень','tagList':[['Federal','Федеральный'],['Municipal','Муниципальный'],['Regional','Региональный']]},'/referenceTest':{'displayName':'Тест выборки','canBeNull':true,'columnSize':'2','tagList':[['01','Региональный'],['02','Муниципальный'],['03','Федеральный'],['04','Региональный']]}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getDynamicPropertyTest()
    {
        DynamicProperty property = dpsHelper.getDynamicProperty(meta.getColumn(meta.getEntity("testTags"), "CODE"))
        assertEquals "CODE", property.getName()
        assertEquals String.class, property.getType()
        assertEquals null, property.getValue()
    }

    @Test
    void addDpForColumnsBase()
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addDpForColumnsBase(dps, meta.getEntity("testTags"), ImmutableList.of("admlevel"))
        assertEquals "{'values':{},'meta':{'/admlevel':{'displayName':'admlevel'}},'order':['/admlevel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void addDpForColumnsBase_with_values()
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addDpForColumnsBase(dps, meta.getEntity("testTags"), ImmutableList.of("admlevel"), ImmutableMap.of("admlevel", "Custom"))
        assertEquals "{'values':{'admlevel':'Custom'},'meta':{'/admlevel':{'displayName':'admlevel'}},'order':['/admlevel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void addDpForColumnsWithoutTags()
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addDpForColumnsWithoutTags(dps, meta.getEntity("testTags"), ImmutableList.of("admlevel"))
        assertEquals "{'values':{'admlevel':'Regional'},'meta':{'/admlevel':{'displayName':'Административный уровень'}},'order':['/admlevel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void addDpForColumnsWithoutTags_with_values()
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addDpForColumnsWithoutTags(dps, meta.getEntity("testTags"), ImmutableList.of("admlevel"), ImmutableMap.of("admlevel", "Custom"))
        assertEquals "{'values':{'admlevel':'Custom'},'meta':{'/admlevel':{'displayName':'Административный уровень'}},'order':['/admlevel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void getDynamicPropertyLocalizationTest()
    {
        def columnDef = meta.getColumn(meta.getEntity("testTags"), "admlevel")
        DynamicProperty property = dpsHelper.getDynamicProperty(columnDef)
        dpsHelper.addMeta(property, columnDef, meta.getEntity("testTags"))

        assertEquals "Административный уровень", property.getDisplayName()
    }

    @Test
    void getDynamicPropertyLocalizationForQueryTest()
    {
        def columnDef = meta.getColumn(meta.getEntity("testTags"), "admlevel")
        DynamicProperty property = dpsHelper.getDynamicProperty(columnDef)
        dpsHelper.addMeta(property, columnDef, meta.getEntity("testTags").getQueries().get("TestLocalizQuery"))

        assertEquals "Test Уровень", property.getDisplayName()
    }

    @Test
    void getDynamicPropertyLocalizationForOperationTest()
    {
        def columnDef = meta.getColumn(meta.getEntity("testTags"), "admlevel")

        DynamicProperty property = dpsHelper.getDynamicProperty(columnDef)
        dpsHelper.addMeta(property, columnDef, meta.getEntity("testTags").getOperations().get("Insert"))

        assertEquals "Уровень", property.getDisplayName()
    }

    @Test
    void getDpsForValuesTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"], [:])

        assertEquals 2, dps.size()
        assertNotNull dps.getProperty("payable").getAttribute(TAG_LIST_ATTR)

        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        dps = new DynamicPropertySetSupport()
        list = dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["payable", "CODE"], [:]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    @Test
    void getDpsForValuesTestOnlyForSpecifiedColumns()
    {
        dps.add(new DynamicProperty("payable", String.class))
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE"], [:])

        assertNull dps.getProperty("payable").getAttribute(TAG_LIST_ATTR)

        dps = new DynamicPropertySetSupport()
        dps.add(new DynamicProperty("payable", String.class))
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("testTags"), ["payable"], [:])

        assertNull dps.getProperty("payable").getAttribute(TAG_LIST_ATTR)
    }

    @Test
    void addDynamicPropertiesTest()
    {
        dpsHelper.addDynamicProperties(dps, meta.getEntity("testTags"), ["CODE", "payable"], [:])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()
        assertEquals "payable", list.get(1).getName()

        dps = new DynamicPropertySetSupport()
        list = dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["payable", "CODE"], [:]).asList()
        assertEquals "payable", list.get(0).getName()
        assertEquals "CODE", list.get(1).getName()
    }

    /**
     * without SQLException - Column "TESTTAGS.FILTERCOLUMN" not found
     */
    @Test
    void addTagsFilterOnlyForTableModel()
    {
        dpsHelper.addDpForColumnsWithoutTags(dps, meta.getEntity("testTags"), ["referenceTest"])

        dpsHelper.addTags(dps, meta.getEntity("testTags"), ["referenceTest"], [filterColumn: "value", "_search_": "true"])
    }

    @Test
    void getDpsForColumnsTestWithValues()
    {
        def presetValues = [notContainColumn: "2", testLong: "3", payable: "no"]
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"], presetValues, [:])

        assertEquals 2, dps.size()
        def list = dps.asList()
        assertEquals "CODE", list.get(0).getName()

        assertEquals "payable", list.get(1).getName()
        assertEquals "no", list.get(1).getValue()

//        assertEquals "notContainColumn", list.get(2).getName()
//        assertEquals String, params.getProperty("notContainColumn").getType()
//        assertEquals "2", params.getProperty("notContainColumn").getValue()

//        assertEquals "testLong", list.get(2).getName()
//        assertEquals Long, params.getProperty("testLong").getType()
//        assertEquals "3", params.getProperty("testLong").getValue()
    }

    @Test
    void getDpsForColumnsTestSpecial()
    {
        def presetValues = [notContainColumn: "2", testLong: "3", payable: "no"]
        dpsHelper.addDpForColumns(dps, meta.getEntity("meters"), ["name"], presetValues)

    }

    @Test
    void getValuesTest()
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"), [:])
        dps.setValue("name", "TestName")
        assertArrayEquals([null, "TestName", null] as Object[], dpsHelper.getValues(dps))
    }

    @Test
    void getColumnsWithoutSpecialTest()
    {
        assertEquals(["ID", "name", "value"] as Set,
                dpsHelper.getColumnsWithoutSpecial(meta.getEntity("meters")).keySet())
    }

    @Test
    void getDpsTest()
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"), [:])
        assertNotNull dps.getProperty("value")

        dps = new DynamicPropertySetSupport()
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("meters"), Collections.singletonList("value"), [:])
        assertNull dps.getProperty("value")
    }

    @Test
    void addDpExcludeColumns_operationParams()
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"), [:])
        assertNotNull dps.getProperty("value")

        dps = new DynamicPropertySetSupport()
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("meters"), ImmutableList.of("payable"), ["value": "1"], ["value": "2"])
        assertEquals "1", dps.getValue("value")
    }

    @Test
    void addDpExcludeColumns_operationParams_not_affected()
    {
        dpsHelper.addDp(dps, meta.getEntity("meters"), [:])
        assertNotNull dps.getProperty("value")

        dps = new DynamicPropertySetSupport()
        dpsHelper.addDpExcludeColumns(dps, meta.getEntity("meters"), ImmutableList.of("payable"), ["CODE": "1"], ["value": "2"])
        assertEquals "2", dps.getValue("value")
    }

    @Test
    void getLabelAndGetLabelRawTest()
    {
        def dps = new DynamicPropertySetSupport()
        dpsHelper.addLabel(dps, "test")
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','readOnly':true,'canBeNull':true,'labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dpsHelper.addLabelRaw(dps, "test")
        assertEquals "{'values':{'infoLabel':'test'},'meta':{'/infoLabel':{'displayName':'infoLabel','rawValue':true,'readOnly':true,'canBeNull':true,'labelField':true}},'order':['/infoLabel']}",
                oneQuotes(JsonFactory.dps(dps).toString())
        dps.remove("infoLabel")

        dpsHelper.addLabel(dps, "customName", "test")
        assertEquals "{'/customName':{'displayName':'customName','readOnly':true,'canBeNull':true,'labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
        dps.remove("customName")

        dpsHelper.addLabelRaw(dps, "customName", "test")
        assertEquals "{'/customName':{'displayName':'customName','rawValue':true,'readOnly':true,'canBeNull':true,'labelField':true}}", oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getAsMapTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"], [:])

        assertEquals([CODE: null, payable: "yes", test: 2], dpsHelper.getAsMap(dps) << [test: 2])

        assertEquals([CODE: "12", payable: "yes"], dpsHelper.getAsMap(dps, [CODE: "12"]))
    }

    @Test
    void setOperationParamsTest()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTags"), ["CODE", "payable"], [:])

        dpsHelper.setOperationParams(dps, [payable: "no"])

        assertEquals([CODE: null, payable: "no"], dpsHelper.getAsMap(dps))

        assertTrue dps.getProperty("payable").getBooleanAttribute(BeanInfoConstants.READ_ONLY)
    }

    @Test
    void testInt()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTypes"), ["testInt"], [:])
        assertEquals "{'/testInt':{'displayName':'testInt','type':'Integer','canBeNull':true,'validationRules':[" +
                "{'attr':{'max':'2147483647','min':'-2147483648'},'type':'range'}," +
                "{'attr':'1','type':'step'}]}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void testBigInt()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTypes"), ["testBigInt"], [:])
        assertEquals "{'/testBigInt':{'displayName':'testBigInt','type':'Long','canBeNull':true,'validationRules':[" +
                "{'attr':{'max':'9223372036854775807','min':'-9223372036854775808'},'type':'range'}," +
                "{'attr':'1','type':'step'}]}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void testTypesCURRENCY()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTypes"), ["payment"], [:])
        assertEquals "{'/payment':{'displayName':'payment','type':'Double','validationRules':[" +
                "{'attr':{'max':'1000000000000000000','min':'-1000000000000000000'},'type':'range'}," +
                "{'attr':'0.01','type':'step'}]}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void testTypesDECIMAL()
    {
        dpsHelper.addDpForColumns(dps, meta.getEntity("testTypes"), ["decimal"], [:])
        assertEquals "{'/decimal':{'displayName':'decimal','type':'Double','validationRules':[" +
                "{'attr':{'max':'10000000000','min':'-10000000000'},'type':'range'}," +
                "{'attr':'1.0E-4','type':'step'}]}}",
                oneQuotes(JsonFactory.dpsMeta(dps).toString())
    }

    @Test
    void getRangeTest()
    {
        assertEquals "{'attr':{'max':'1000000000','min':'0'},'type':'range'}",
                oneQuotes(jsonb.toJson(dpsHelper.getRange(9, true)))

        assertEquals "{'attr':{'max':'1000000000000000000','min':'-1000000000000000000'},'type':'range'}",
                oneQuotes(jsonb.toJson(dpsHelper.getRange(18, false)))

        assertEquals "{'attr':{'max':'1.0E300','min':'-1.0E300'},'type':'range'}",
                oneQuotes(jsonb.toJson(dpsHelper.getRange(300, false)))
    }

    @Test
    void getPrecisionTest()
    {
        assertEquals "1", dpsHelper.getPrecision(0)
        assertEquals "0.1", dpsHelper.getPrecision(1)
        assertEquals "0.01", dpsHelper.getPrecision(2)
        assertEquals "0.001", dpsHelper.getPrecision(3)
        for (int i = 4; i <= 18; i++) {
            assertEquals "1.0E-${i}".toString(), dpsHelper.getPrecision(i)
        }
        assertEquals "1.0E-300", dpsHelper.getPrecision(300)
    }

    @Test
    void addParamsFromQuery()
    {
        dpsHelper.addParamsFromQuery(dps, meta.getEntity("testTags"),
                meta.getQuery("testTags", "With parameter"), [:])
        assertEquals "{'values':{'payable':'yes'},'meta':{'/payable':{'displayName':'Оплачиваемая','canBeNull':true,'tagList':[['yes','да'],['no','нет']]}},'order':['/payable']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void addParamsFromQueryWithNotEntityParameter()
    {
        dpsHelper.addParamsFromQuery(dps, meta.getEntity("testTags"),
                meta.getQuery("testTags", "With Not entity parameter"), [:])
        assertEquals "{'values':{},'meta':{'/queryString':{'displayName':'queryString'}},'order':['/queryString']}",
                oneQuotes(JsonFactory.dps(dps).toString())
    }

    @Test
    void addTagsFromCustomSelectionViewWithParamTest()
    {
        def property = new DynamicProperty("test", String.class)
        dpsHelper.addTags(property,
                meta.getColumn("columnWithCustomViewName", "referenceTest"),
                ImmutableMap.of())
        assertEquals("customRegional", ((String[][])property.getAttribute(TAG_LIST_ATTR))[0][1])
    }

    @Test
    void addTagsForPrimaryKeyValue()
    {
        def property = new DynamicProperty("test", String.class)
        dpsHelper.addTags(property,
                meta.getColumn("testTags", "referenceTest"),
                ImmutableMap.of("CODE", "02"))
        assertEquals("Муниципальный", ((String[][])property.getAttribute(TAG_LIST_ATTR))[0][1])
    }
}
