package com.developmentontheedge.be5.api.helpers

import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.test.Be5ProjectTest
import com.developmentontheedge.sql.model.AstStart
import com.developmentontheedge.sql.model.SqlQuery
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.*


class FilterHelperTest extends Be5ProjectTest
{
    @Inject FilterHelper filterHelper
    @Inject Meta meta

    @Test
    void empty()
    {
        AstStart ast = SqlQuery.parse(meta.getQueryIgnoringRoles("filterTestTable", "Simple").getQueryCompiled().validate().trim())
        filterHelper.applyFilters(ast, "filterTestTable", [:])

        assertEquals("SELECT ft.name, ft.value\n" +
                     "FROM filterTestTable ft", ast.format())
    }

    @Test
    void simpleFilterIntColumn()
    {
        AstStart ast = SqlQuery.parse(meta.getQueryIgnoringRoles("filterTestTable", "Simple").getQueryCompiled().validate().trim())
        filterHelper.applyFilters(ast, "filterTestTable", ["value":123])

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.value = 123", ast.format())
    }

    @Test
    void simpleFilterStringColumn()
    {
        AstStart ast = SqlQuery.parse(meta.getQueryIgnoringRoles("filterTestTable", "Simple").getQueryCompiled().validate().trim())
        filterHelper.applyFilters(ast, "filterTestTable", ["name":"test"])

        assertEquals("SELECT ft.name, ft.value\n" +
                "FROM filterTestTable ft WHERE ft.name ='test'", ast.format())
    }
}