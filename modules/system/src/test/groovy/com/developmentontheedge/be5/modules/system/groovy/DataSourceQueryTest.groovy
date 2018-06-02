package com.developmentontheedge.be5.modules.system.groovy

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.modules.system.SystemBe5ProjectTest

import javax.inject.Inject
import org.junit.Test


class DataSourceQueryTest extends SystemBe5ProjectTest
{
    @Inject Meta meta

    @Test
    void get() throws Exception
    {
        meta.getQuery("_system_", "DataSource");
//        TableModel tableModel = new DataSource().initialize(
//                injector.getMeta().getQuery("_system_", "DataSource Parameters"),
//                new HashMap<>(),
//                getMockRequest(""),
//                injector
//        ).get();
//        assertTrue(tableModel.getRows().size() > 0);
    }

}