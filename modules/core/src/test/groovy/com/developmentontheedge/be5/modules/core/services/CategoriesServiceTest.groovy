package com.developmentontheedge.be5.modules.core.services

import com.developmentontheedge.be5.server.services.CategoriesService
import com.developmentontheedge.be5.modules.core.controllers.CoreBe5ProjectDBTest

import javax.inject.Inject
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals


class CategoriesServiceTest extends CoreBe5ProjectDBTest
{
    @Inject CategoriesService categoriesService

    @Before
    void setUp() throws Exception
    {
        db.update("DELETE FROM categories")

        database.categories.add([
                entity  : "anotherCategory",
                name    : "another root"
        ])
    }

    @Test
    void empty()
    {
        def docTypes = categoriesService.getCategoriesForest("docTypes", false)
        assertEquals(0, docTypes.size())
    }

    @Test
    void test()
    {
        database.categories.add([
                entity  : "docTypes",
                name    : "Root"
        ])

        def docTypes = categoriesService.getCategoriesForest("docTypes", false)
        assertEquals(1, docTypes.size())
        assertEquals("Root", docTypes.get(0).name)
    }

    @Test
    void snapshot()
    {
        def rootID = database.categories.add([
                entity  : "docTypes",
                name    : "Root"
        ])
        def p1ID = database.categories.add([
                entity  : "docTypes",
                name    : "p1",
                parentID: rootID
        ])
        def p2ID = database.categories.add([
                entity  : "docTypes",
                name    : "p2",
                parentID: rootID
        ])
        def c1ID = database.categories.add([
                entity  : "docTypes",
                name    : "c1",
                parentID: p1ID
        ])
        def docTypes = categoriesService.getCategoriesForest("docTypes", false)

        assertEquals("[" +
                "{'children':[" +
                    "{'children':[" +
                        "{'children':[" +
                        "],'id':${c1ID},'name':'c1'}" +
                    "],'id':${p1ID},'name':'p1'}," +
                    "{'children':[" +
                    "],'id':${p2ID},'name':'p2'}" +
                "],'id':${rootID},'name':'Root'}]", oneQuotes(jsonb.toJson(docTypes)))
    }

    @Test
    void testhideEmpty()
    {
        def rootID = database.categories.add([
                entity  : "docTypes",
                name    : "Root"
        ])
        database.categories.add([
                entity  : "docTypes",
                name    : "p1",
                parentID: rootID
        ])

        def docTypes = categoriesService.getCategoriesForest("docTypes", true)
        assertEquals(0, docTypes.size())

        database.classifications.add([
                categoryID: rootID,
                recordID: "docTypes.1"
        ])

        docTypes = categoriesService.getCategoriesForest("docTypes", true)

        assertEquals(1, docTypes.size())
        assertEquals("Root", docTypes.get(0).name)
    }

    @Test
    void getCategoryNavigationTest()
    {
        Long rootID = database.categories.add([
                entity  : "docTypes",
                name    : "Root"
        ])

        Long p1ID = database.categories.add([
                entity  : "docTypes",
                name    : "p1",
                parentID: rootID
        ])
        database.categories.add([
                entity  : "docTypes",
                name    : "p2",
                parentID: rootID
        ])

        database.categories.add([
                entity  : "docTypes",
                name    : "c1",
                parentID: p1ID
        ])
        database.categories.add([
                entity  : "docTypes",
                name    : "c2",
                parentID: p1ID
        ])

        def docTypes = categoriesService.getCategoryNavigation("docTypes", p1ID)

        assertEquals 1, docTypes.size()
        assertEquals "Root", docTypes.get(0).name

        assertEquals 1, docTypes.get(0).children.size()
        assertEquals "p1", docTypes.get(0).children.get(0).name

        assertEquals 2, docTypes.get(0).children.get(0).children.size()
        assertEquals "c2", docTypes.get(0).children.get(0).children.get(0).name
        assertEquals "c1", docTypes.get(0).children.get(0).children.get(1).name

        assertEquals 0, docTypes.get(0).children.get(0).children.get(0).children.size()
        assertEquals 0, docTypes.get(0).children.get(0).children.get(1).children.size()
    }
}