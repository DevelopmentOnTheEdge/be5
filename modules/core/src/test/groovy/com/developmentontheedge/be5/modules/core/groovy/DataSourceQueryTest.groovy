package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.modules.core.controllers.CoreBe5ProjectDBTest

import javax.inject.Inject
import org.junit.Test

class DataSourceQueryTest extends CoreBe5ProjectDBTest
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