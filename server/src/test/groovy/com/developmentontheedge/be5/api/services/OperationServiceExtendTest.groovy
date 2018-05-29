package com.developmentontheedge.be5.api.services

import com.developmentontheedge.be5.base.services.UserAwareMeta
import com.developmentontheedge.be5.base.services.ProjectProvider
import com.developmentontheedge.be5.operation.services.GroovyOperationLoader

import javax.inject.Inject
import com.developmentontheedge.be5.test.SqlMockOperationTest
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.*


@TypeChecked
class OperationServiceExtendTest extends SqlMockOperationTest
{
    @Inject UserAwareMeta userAwareMeta
    @Inject GroovyOperationLoader groovyOperationLoader
    @Inject ProjectProvider projectProvider

    @Test
    void getSuperOperationSimpleNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")

        assertEquals("CustomOperation", groovyOperationLoader.getSimpleSuperClassName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")

        assertEquals("OperationWithExtend", groovyOperationLoader.getSimpleSuperClassName(operation))
    }

    @Test
    void getSuperOperationCanonicalNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals("testtableAdmin.CustomOperation.groovy",
                groovyOperationLoader.getCanonicalSuperClassName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")
        assertEquals("testtableAdmin.OperationWithExtend.groovy",
                groovyOperationLoader.getCanonicalSuperClassName(operation))
    }

    @Test
    void testLoadSuperOperation()
    {
        projectProvider.reloadProject()

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals(["testtableAdmin.CustomOperation.groovy"], groovyOperationLoader.preloadSuperOperation(operation))
    }

}