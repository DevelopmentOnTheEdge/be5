package com.developmentontheedge.be5.modules.core.groovy;

import com.developmentontheedge.be5.components.impl.model.TableModel;
import com.developmentontheedge.be5.metadata.model.Query;
import com.developmentontheedge.be5.test.AbstractProjectIntegrationH2Test;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertTrue;


public class DataSourceQueryTest extends AbstractProjectIntegrationH2Test
{
    @Test
    public void get() throws Exception
    {
        Query query = injector.getMeta()
                .getQueryIgnoringRoles("_system_", "DataSource");
//        TableModel tableModel = new DataSource().initialize(
//                injector.getMeta().getQueryIgnoringRoles("_system_", "DataSource Parameters"),
//                new HashMap<>(),
//                getMockRequest(""),
//                injector
//        ).get();
//        assertTrue(tableModel.getRows().size() > 0);
    }

}