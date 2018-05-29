package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.base.services.Meta

import javax.inject.Inject
import com.developmentontheedge.be5.testutils.TestTableQueryDBTest
import groovy.transform.TypeChecked
import org.junit.Test

import static com.developmentontheedge.be5.metadata.model.EntityType.COLLECTION
import static com.developmentontheedge.be5.metadata.model.EntityType.GENERIC_COLLECTION
import static org.junit.Assert.assertEquals


@TypeChecked
class MetaTest extends TestTableQueryDBTest
{
    @Inject private Meta meta

    @Test
    void getTableReferences()
    {
        meta.getTableReferences(COLLECTION).size()

        assertEquals 1, meta.getTableReferences(COLLECTION).size()
        assertEquals 2, meta.getTableReferences(GENERIC_COLLECTION).size()

        assertEquals "testGenCollection", meta.getTableReferences(GENERIC_COLLECTION).get(0).getTableFrom()
        assertEquals "recordID", meta.getTableReferences(GENERIC_COLLECTION).get(0).getColumnsFrom()
        assertEquals null, meta.getTableReferences(GENERIC_COLLECTION).get(0).getTableTo()
        assertEquals null, meta.getTableReferences(GENERIC_COLLECTION).get(0).getColumnsTo()

        assertEquals "testGenCollection", meta.getTableReferences(GENERIC_COLLECTION).get(1).getTableFrom()
        assertEquals "categoryID", meta.getTableReferences(GENERIC_COLLECTION).get(1).getColumnsFrom()
        assertEquals "testtable", meta.getTableReferences(GENERIC_COLLECTION).get(1).getTableTo()
        assertEquals "ID", meta.getTableReferences(GENERIC_COLLECTION).get(1).getColumnsTo()
    }
}