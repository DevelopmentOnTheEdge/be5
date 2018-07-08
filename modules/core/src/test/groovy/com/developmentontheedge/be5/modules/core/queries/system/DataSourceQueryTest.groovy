package com.developmentontheedge.be5.modules.core.queries.system

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest
import org.junit.Test

import javax.inject.Inject

class DataSourceQueryTest extends CoreBe5ProjectDBTest {
    @Inject Meta meta

    @Test
    void get()
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