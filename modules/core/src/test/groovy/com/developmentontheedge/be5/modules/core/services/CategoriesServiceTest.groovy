package com.developmentontheedge.be5.modules.core.services

import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.Be5ProjectDBTest
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals


class CategoriesServiceTest extends Be5ProjectDBTest
{
    @Inject CategoriesService categoriesService

    @Before
    void setUp() throws Exception
    {
        db.update("DELETE FROM categories")
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
        def parentID = database.categories.add([
                entity  : "docTypes",
                name    : "Root",
                parentID: "0"
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
                name    : "Root",
                parentID: "0"
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
                name    : "Root",
                parentID: "0"
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

}