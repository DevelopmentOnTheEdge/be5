package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.inject.Inject
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Test

class DataSourceQueryTest extends Be5ProjectDBTest
{
    @Inject Meta meta

    @Test
    void get() throws Exception
    {
        meta.getQueryIgnoringRoles("_system_", "DataSource");
//        TableModel tableModel = new DataSource().initialize(
//                injector.getMeta().getQueryIgnoringRoles("_system_", "DataSource Parameters"),
//                new HashMap<>(),
//                getMockRequest(""),
//                injector
//        ).get();
//        assertTrue(tableModel.getRows().size() > 0);
    }

}