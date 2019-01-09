package com.developmentontheedge.be5.operation.services

import com.developmentontheedge.be5.base.meta.ProjectProvider
import com.developmentontheedge.be5.base.meta.UserAwareMeta
import com.developmentontheedge.be5.metadata.RoleType
import com.developmentontheedge.be5.metadata.model.GroovyOperation
import com.developmentontheedge.be5.operation.OperationsSqlMockProjectTest
import groovy.transform.TypeChecked
import org.junit.Before
import org.junit.Test

import javax.inject.Inject

import static org.junit.Assert.assertEquals


@TypeChecked
class OperationServiceExtendTest extends OperationsSqlMockProjectTest
{
    @Inject
    UserAwareMeta userAwareMeta
    @Inject
    GroovyOperationLoader groovyOperationLoader
    @Inject
    ProjectProvider projectProvider

    @Before
    void setUp()
    {
        setStaticUserInfo(RoleType.ROLE_SYSTEM_DEVELOPER)
    }

    @Test
    void getSuperOperationSimpleNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")

        assertEquals("CustomOperation", groovyOperationLoader.getSimpleSuperClassName((GroovyOperation) operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")

        assertEquals("OperationWithExtend", groovyOperationLoader.getSimpleSuperClassName((GroovyOperation) operation))
    }

    @Test
    void getSuperOperationCanonicalNameTest()
    {
        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals("testtableAdmin.CustomOperation.groovy",
                groovyOperationLoader.getCanonicalSuperClassName((GroovyOperation) operation))

        operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend2")
        assertEquals("testtableAdmin.OperationWithExtend.groovy",
                groovyOperationLoader.getCanonicalSuperClassName((GroovyOperation) operation))
    }

    @Test
    void testLoadSuperOperation()
    {
        projectProvider.reloadProject()

        def operation = userAwareMeta.getOperation("testtableAdmin", "OperationWithExtend")
        assertEquals(["testtableAdmin.CustomOperation.groovy"], groovyOperationLoader.
                preloadSuperOperation((GroovyOperation) operation))
    }

}
