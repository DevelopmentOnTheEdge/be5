package com.developmentontheedge.be5.modules.core.queries.system

import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.modules.core.CoreBe5ProjectDBTest
import com.developmentontheedge.be5.query.model.CellModel
import com.developmentontheedge.be5.query.services.TableModelService
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class QueriesTest extends CoreBe5ProjectDBTest
{
    @Inject
    TableModelService tableModelService

    @Before
    void setUp()
    {
        initUserWithRoles(RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @Test
    void getEntities()
    {
        Query query = meta.getQuery("_system_", "Entities")

        def table = tableModelService.create(query, Collections.emptyMap())

        assertTrue(table.getRows().stream()
                .filter({ x -> ((CellModel) x.cells.get(0)).getContent() == "_system_" })
                .findFirst().present)
    }

    @Test
    void getSessionVariables()
    {
        session.set("test", "value")
        Query query = meta.getQuery("_system_", "Session variables")

        def table = tableModelService.create(query, Collections.emptyMap())

        assertEquals(true, table.getRows().stream()
                .map({ x -> x.id })
                .filter({ x -> x.equals("test") })
                .findFirst().isPresent())

//        assertEquals("test", ((TableModel.CellModel)table.getRows().get(0).cells.get(0)).content)
//        assertEquals("value", ((TableModel.CellModel)table.getRows().get(0).cells.get(1)).content)
    }

}
