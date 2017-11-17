package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.helpers.UserAwareMeta
import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.SqlMockOperationTest
import groovy.transform.TypeChecked
import org.junit.Test

import static org.junit.Assert.*


@TypeChecked
class OperationServiceImplTest extends SqlMockOperationTest
{
    @Inject UserAwareMeta userAwareMeta
    @Inject GroovyOperationLoader operationService
    @Inject ProjectProvider projectProvider

    @Test
    void getSuperOperationSimpleNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")

        assertEquals("CustomOperation", operationService.getSimpleSuperClassName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")

        assertEquals("OperationWithExtend", operationService.getSimpleSuperClassName(operation))
    }

    @Test
    void getSuperOperationCanonicalNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals("testtableAdmin.CustomOperation.groovy",
                operationService.getCanonicalSuperClassName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")
        assertEquals("testtableAdmin.OperationWithExtend.groovy",
                operationService.getCanonicalSuperClassName(operation))
    }

    @Test
    void testLoadSuperOperation()
    {
        projectProvider.reloadProject()

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals(["testtableAdmin.CustomOperation.groovy"], operationService.preloadSuperOperation(operation))
    }

}