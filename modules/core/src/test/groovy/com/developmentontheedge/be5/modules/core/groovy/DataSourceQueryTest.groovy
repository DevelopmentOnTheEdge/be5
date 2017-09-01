package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test
import org.junit.Test

import java.util.HashMap

import static org.junit.Assert.assertTrue


class DataSourceQueryTest extends AbstractProjectIntegrationH2Test
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