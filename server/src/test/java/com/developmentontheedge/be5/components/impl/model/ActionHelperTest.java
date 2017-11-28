package com.developmentontheedge.be5.components.impl.model;

import com.developmentontheedge.be5.api.services.Meta;
import com.developmentontheedge.be5.env.Inject;
import com.developmentontheedge.be5.metadata.QueryType;
import com.developmentontheedge.be5.metadata.model.Entity;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActionHelperTest extends Be5ProjectTest
{
    @Inject private Meta meta;

    @Test
    public void legacyUrlTest() throws Exception
    {
        Query query = getQuery(QueryType.STATIC, "welfareGroups.redir?_qn_=Муниципальные+услуги");
        assertEquals("table/welfareGroups/Муниципальные услуги",
                ActionHelper.toAction(query).arg);
    }

    @Test
    public void legacyFormUrlTest() throws Exception
    {
        Query query = getQuery(QueryType.STATIC, "public.households.redir?_qn_=Account+Balance&_on_=OpenReview4Period");
        assertEquals("form/public.households/Account Balance/OpenReview4Period",
                ActionHelper.toAction(query).arg);
    }

    @Test
    public void legacyUrlTest2() throws Exception
    {
        Query query = getQuery(QueryType.STATIC, "mspReceiverCategories.redir");

        assertEquals("table/mspReceiverCategories",
                ActionHelper.toAction(query).arg);
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