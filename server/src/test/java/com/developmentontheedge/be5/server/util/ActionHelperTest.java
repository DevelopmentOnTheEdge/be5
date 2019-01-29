package com.developmentontheedge.be5.server.util;

import com.developmentontheedge.be5.meta.Meta;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.ServerBe5ProjectTest;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;


public class ActionHelperTest extends ServerBe5ProjectTest
{
    @Inject
    private Meta meta;

    @Test
    public void legacyUrlTest()
    {
        Query query = getQuery(QueryType.STATIC, "welfareGroups.redir?_qn_=Муниципальные+услуги");
        assertEquals("table/welfareGroups/Муниципальные услуги",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void legacyFormUrlTest()
    {
        Query query = getQuery(QueryType.STATIC, "public.households.redir?_qn_=Account+Balance&_on_=OpenReview4Period");
        assertEquals("form/public.households/Account Balance/OpenReview4Period",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void legacyUrlTest2()
    {
        Query query = getQuery(QueryType.STATIC, "mspReceiverCategories.redir");

        assertEquals("table/mspReceiverCategories/All records",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void legacyUrlTestWithParams()
    {
        Query query = getQuery(QueryType.STATIC, "welfareGroups.redir?_qn_=Муниципальные+услуги&value=1");
        assertEquals("table/welfareGroups/Муниципальные услуги/value=1",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void qLegacyUrl()
    {
        Query query = getQuery(QueryType.STATIC, "q?_t_=downloads");
        assertEquals("table/downloads/All records",
                ActionUtils.toAction(query).arg);

        Query query2 = getQuery(QueryType.STATIC, "q?_t_=downloads&_qn_=My subscriptions");
        assertEquals("table/downloads/My subscriptions",
                ActionUtils.toAction(query2).arg);
    }

    @Test
    public void oLegacyUrl()
    {
        Query query = getQuery(QueryType.STATIC, "o?_t_=tariffs&_on_=Insert&productID=12");
        assertEquals("form/tariffs/All records/Insert/productID=12",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void staticPage()
    {
        Query query = getQuery(QueryType.STATIC, "page.be");
        assertEquals("static/page.be",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void staticPageWithoutBe()
    {
        Query query = getQuery(QueryType.STATIC, "static/page");
        assertEquals("static/page",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void customPath()
    {
        Query query = getQuery(QueryType.STATIC, "page/contacts");
        assertEquals("page/contacts",
                ActionUtils.toAction(query).arg);
    }

    private Query getQuery(QueryType type, String queryCode)
    {
        Entity entity = meta.getEntity("testtable");
        Query query = new Query("ActionHelperTest", entity);

        query.setType(type);
        query.setQuery(queryCode);
        return query;
    }

}
