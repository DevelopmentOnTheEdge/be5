package com.developmentontheedge.be5.modules.core.groovy

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.query.DocumentGenerator
import com.developmentontheedge.be5.query.impl.model.TableModel
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.metadata.model.Query
import com.developmentontheedge.be5.model.TablePresentation
import com.developmentontheedge.be5.test.Be5ProjectTest
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


class QueriesTest extends Be5ProjectTest
{
    @Inject DocumentGenerator documentGenerator
    @Inject Meta meta

    @Test
    void getEntities()
    {
        Query query = meta.getQueryIgnoringRoles("_system_", "Entities")

        def table = documentGenerator.getTablePresentation(query, Collections.emptyMap())

        assertTrue(table.getRows().stream()
                .filter({ x -> ((TableModel.CellModel)x.cells.get(0)).getContent() == "users"})
                .findFirst().present)
    }

    @Test
    void getSessionVariables()
    {
        setSession("test", "value")
        Query query = meta.getQueryIgnoringRoles("_system_", "Session variables")

        def table = documentGenerator.getTablePresentation(query, Collections.emptyMap())

        assertEquals("test", table.getRows().get(0).id)

        assertEquals("test", ((TableModel.CellModel)table.getRows().get(0).cells.get(0)).content)
        assertEquals("value", ((TableModel.CellModel)table.getRows().get(0).cells.get(1)).content)
    }

}