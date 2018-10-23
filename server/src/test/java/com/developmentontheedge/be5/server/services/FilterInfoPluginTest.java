package com.developmentontheedge.be5.server.services;

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
        setStaticUserInfo(RoleType.ROLE_GUEST);
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
        Assert.assertEquals("{'operationParamsInfo':{'Тест выборки':'Региональный'}}",
                oneQuotes(jsonb.toJson(resourceData.getAttributes())));
    }

}
