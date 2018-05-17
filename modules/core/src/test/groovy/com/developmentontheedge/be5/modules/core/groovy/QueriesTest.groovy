package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.DocumentGenerator
import com.developmentontheedge.be5.modules.core.controllers.CoreBe5ProjectDBTest
import com.developmentontheedge.be5.query.impl.TableModel
import com.developmentontheedge.be5.metadata.model.Query
import javax.inject.Inject
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


class QueriesTest extends CoreBe5ProjectDBTest
{
    @Inject DocumentGenerator documentGenerator
    @Inject Meta meta

    @Test
    void getEntities()
    {
        Query query = meta.getQuery("_system_", "Entities")

        def table = documentGenerator.getTablePresentation(query, Collections.emptyMap())

        assertTrue(table.getRows().stream()
                .filter({ x -> ((TableModel.CellModel)x.cells.get(0)).getContent() == "users"})
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