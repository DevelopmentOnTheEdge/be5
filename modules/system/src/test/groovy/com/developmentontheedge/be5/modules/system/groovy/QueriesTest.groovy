package com.developmentontheedge.be5.modules.system.groovy

import com.developmentontheedge.be5.base.services.Meta
import com.developmentontheedge.be5.server.services.DocumentGenerator
import com.developmentontheedge.be5.modules.system.SystemBe5ProjectTest
import com.developmentontheedge.be5.query.model.CellModel
import com.developmentontheedge.be5.metadata.model.Query
import javax.inject.Inject
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


class QueriesTest extends SystemBe5ProjectTest
{
    @Inject DocumentGenerator documentGenerator
    @Inject Meta meta

    @Test
    void getEntities()
    {
        Query query = meta.getQuery("_system_", "Entities")

        def table = documentGenerator.getTablePresentation(query, Collections.emptyMap())

        assertTrue(table.getRows().stream()
                .filter({ x -> ((CellModel)x.cells.get(0)).getContent() == "_system_"})
                .findFirst().present)
    }

    @Test
    void getSessionVariables()
    {
        setSession("test", "value")
        Query query = meta.getQuery("_system_", "Session variables")

        def table = documentGenerator.getTablePresentation(query, Collections.emptyMap())

        assertEquals(true, table.getRows().stream()
                .map({x -> x.id})
                .filter({x -> x.equals("test")})
                .findFirst().isPresent())

//        assertEquals("test", ((TableModel.CellModel)table.getRows().get(0).cells.get(0)).content)
//        assertEquals("value", ((TableModel.CellModel)table.getRows().get(0).cells.get(1)).content)
    }

}