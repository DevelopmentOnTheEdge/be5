package com.developmentontheedge.be5.util;

import com.developmentontheedge.be5.base.services.Meta;
import javax.inject.Inject;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.server.test.ServerBe5ProjectTest;
import com.developmentontheedge.be5.server.util.ActionUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActionHelperTest extends ServerBe5ProjectTest
{
    @Inject private Meta meta;

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

        assertEquals("table/mspReceiverCategories",
                ActionUtils.toAction(query).arg);
    }

    @Test
    public void legacyUrlTestWithParams()
    {
        Query query = getQuery(QueryType.STATIC, "welfareGroups.redir?_qn_=Муниципальные+услуги&value=1");
        assertEquals("table/welfareGroups/Муниципальные услуги/value=1",
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