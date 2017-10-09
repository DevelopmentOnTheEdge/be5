package com.developmentontheedge.be5.api.services.impl

import com.developmentontheedge.be5.api.helpers.UserAwareMeta
import com.developmentontheedge.be5.api.services.Meta
import com.developmentontheedge.be5.api.services.OperationService
import com.developmentontheedge.be5.api.services.ProjectProvider
import com.developmentontheedge.be5.env.Inject
import com.developmentontheedge.be5.test.SqlMockOperationTest
import org.junit.Test

import static org.junit.Assert.*


class OperationServiceImplTest extends SqlMockOperationTest
{
    @Inject Meta meta
    @Inject UserAwareMeta userAwareMeta
    @Inject OperationService operationService
    @Inject ProjectProvider projectProvider
    
    @Test
    void selectedRowsTest()
    {
        assertTrue( Arrays.equals(["1","2","3"] as String[], OperationServiceImpl.selectedRows("1,2,3")))

        assertTrue( Arrays.equals(["1"] as String[], OperationServiceImpl.selectedRows("1")))

        assertTrue( Arrays.equals([] as String[], OperationServiceImpl.selectedRows("")))
    }

    @Test
    void getSuperOperationClassNameTest() throws Exception
    {
        operationService = (OperationServiceImpl)operationService

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")

        assertEquals("CustomOperation", operationService.getSuperOperationClassName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")

        assertEquals("OperationWithExtend", operationService.getSuperOperationClassName(operation))
    }

    @Test
    void getSuperOperationClassFullNameTest() throws Exception
    {
        operationService = (OperationServiceImpl)operationService

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals("testtableAdmin.CustomOperation.groovy",
                operationService.getSuperOperationFullName(operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")
        assertEquals("testtableAdmin.OperationWithExtend.groovy",
                operationService.getSuperOperationFullName(operation))
    }

    @Test
    void testLoadSuperOperation()
    {
        projectProvider.reloadProject()
        operationService = (OperationServiceImpl)operationService

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals(["testtableAdmin.CustomOperation.groovy"], operationService.preloadSuperOperation(operation))
    }

}