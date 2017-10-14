package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.test.Be5ProjectTest;
import org.junit.Test

class EntitiesTest extends Be5ProjectTest
{
    @Test
    void get() throws Exception
    {
//todo refactoring test as operation test - refactoring QueryRouter to service
//        Query query = injector.getMeta()
//                .getQueryIgnoringRoles("_system_", "Entities");
//        try
//        {
//            TableBuilder tableBuilder = (TableBuilder) GroovyRegister.parseClass(query.getQuery()).newInstance();
//
//            TableModel tableModel = tableBuilder
//                    .initialize(query, new HashMap<>(), getMockRequest(""))
//                    .getTable();
//
//            assertTrue(tableModel.getRows().size() > 0);
//        }
//        catch( NoClassDefFoundError | IllegalAccessException | InstantiationException e )
//        {
//            throw Be5Exception.internal(e);
//        }
    }

}