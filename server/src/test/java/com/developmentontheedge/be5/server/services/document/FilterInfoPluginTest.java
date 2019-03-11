package com.developmentontheedge.be5.server.services.document;

import com.developmentontheedge.be5.metadata.RoleType;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.model.jsonapi.ResourceData;
import com.developmentontheedge.be5.test.ServerBe5ProjectDBTest;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Collections;

public class FilterInfoPluginTest extends ServerBe5ProjectDBTest
{
    @Inject
    private FilterInfoPlugin filterInfoPlugin;

    @Before
    public void setUp()
    {
        initUserWithRoles(RoleType.ROLE_ADMINISTRATOR);
        database.getEntity("testTags").add(ImmutableMap.of("CODE", "50"));
    }

    @After
    public void tearDown() throws Exception
    {
        database.getEntity("testTags").removeBy(ImmutableMap.of("CODE", "50"));
    }

    @Test
    public void addData()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("referenceTest", "50"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Тест выборки','value':'Региональный'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void addDataCustomParam()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("customParam", "50"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Кастомный параметер','value':'50'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void entity()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, ImmutableMap.of("entity", "testTags", "entityID", "1"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Property Types','value':'Региональный'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void addDataForPrimaryKey()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("CODE", "50"));
        Assert.assertEquals("{'operationParamsInfo':[{'value':'Региональный'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void addDataForEnum()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("admlevel", "Municipal"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Административный уровень','value':'Муниципальный'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void addDataForBool()
    {
        Query query = meta.getQuery("testTags", "All records");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("payable", "no"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Оплачиваемая','value':'нет'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void addDataUsedParam()
    {
        Query query = meta.getQuery("testtable", "usedParam filter info");
        ResourceData resourceData = filterInfoPlugin.addData(query, Collections.singletonMap("referenceTest", "50"));
        Assert.assertEquals("{'operationParamsInfo':[{'key':'Тест выборки','value':'Региональный'}]}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

    @Test
    public void sqlSubQuery()
    {
        Query query = meta.getQuery("testtable", "sqlSubQuery");
        filterInfoPlugin.addData(query, Collections.singletonMap("name", "value"));
    }
}
